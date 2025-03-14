package com.example.stayfinder.service.booking;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.exception.DataProcessingException;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.BookingMapper;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.accommodation.AccommodationRepository;
import com.example.stayfinder.repository.booking.BookingRepository;
import com.example.stayfinder.repository.booking.BookingSpecificationBuilder;
import com.example.stayfinder.repository.user.UserRepository;
import com.example.stayfinder.service.notification.NotificationService;
import com.example.stayfinder.service.payment.StripePaymentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingSpecificationBuilder specificationBuilder;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final StripePaymentService paymentService;
    private final NotificationService notificationService;

    @Override
    public BookingDto save(User user, CreateBookingRequestDto requestDto) {
        if (paymentService.existsByBookingUserIdAndStatus(user.getId())) {
            throw new DataProcessingException("The user has unpaid reservations!");
        }
        Accommodation accommodationFromDb = validateAccommodation(requestDto);
        User userFromDb = findUserById(user.getId());
        Booking booking = createBookingEntity(requestDto, accommodationFromDb, userFromDb);
        notificationService.sendCreateBookingMessage(accommodationFromDb, userFromDb, booking);

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public Page<BookingDto> findAllByUserIdAndStatus(
            BookingFilterParameters parameters, Pageable pageable) {
        Specification<Booking> specification = specificationBuilder.build(parameters);
        Page<Booking> bookingsPage = bookingRepository.findAll(specification, pageable);
        if (bookingsPage.isEmpty()) {
            throw new EntityNotFoundException("No bookings found for the specified filters.");
        }
        return bookingsPage.map(bookingMapper::toDto);
    }

    @Override
    public Page<BookingDto> findAllByUserId(Long userId, Pageable pageable) {
        Page<Booking> bookingsPage = bookingRepository.findByUserId(userId, pageable);
        if (bookingsPage.isEmpty()) {
            throw new EntityNotFoundException("No bookings found for user id: " + userId);
        }
        return bookingsPage.map(bookingMapper::toDto);
    }

    @Override
    public BookingDto findByUserIdAndId(Long userId, Long bookingId) {
        Booking bookingFromDb = findBookingByUserAndId(userId, bookingId);
        return bookingMapper.toDto(bookingFromDb);
    }

    @Override
    public BookingDto updateByUserIdAndId(
            Long userId, Long bookingId, CreateBookingRequestDto requestDto) {
        Booking bookingFromDb = findBookingByUserAndId(userId, bookingId);
        validateBookingUpdate(requestDto, bookingFromDb);

        bookingMapper.updateEntityFromDto(requestDto, bookingFromDb);
        return bookingMapper.toDto(bookingRepository.save(bookingFromDb));
    }

    @Override
    public void cancelByUserIdAndId(Long userId, Long bookingId) {
        Booking bookingFromDb = findBookingByUserAndId(userId, bookingId);
        if (bookingFromDb.getStatus() != Booking.Status.CANCELED) {
            bookingRepository.updateStatus(bookingId, Booking.Status.CANCELED);
            notificationService.sendCancelBookingMessage(findUserById(userId), bookingFromDb);
        } else {
            throw new DataProcessingException("Booking is already canceled.");
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Override
    public void checkHourlyExpiredBookings() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        Set<Long> bookingIds = bookingRepository.findByCheckOutDateAndStatusNot(
                        now, Booking.Status.CANCELED).stream()
                .map(Booking::getId)
                .collect(Collectors.toSet());
        bookingRepository.updateStatusForExpiredBooking(bookingIds, Booking.Status.EXPIRED);

        List<User> userList = userRepository.findAllById(bookingIds);

        if (!bookingIds.isEmpty()) {
            notificationService.sendReleaseAccommodationMessage(bookingIds, userList);
        }
    }

    private Accommodation validateAccommodation(CreateBookingRequestDto requestDto) {
        List<Booking> overlappingBookings = bookingRepository.findByAccommodationId(
                        requestDto.accommodationId()).stream()
                .filter(b -> isOverlapping(b, requestDto))
                .toList();

        checkAvailability(requestDto, overlappingBookings);
        return findAccommodationById(requestDto.accommodationId());
    }

    private void validateBookingUpdate(
            CreateBookingRequestDto requestDto, Booking bookingFromDb) {
        List<Booking> overlappingBookings = bookingRepository.findByAccommodationId(
                        requestDto.accommodationId()).stream()
                .filter(b -> isOverlapping(b, requestDto))
                .filter(b -> !b.getId().equals(bookingFromDb.getId()))
                .toList();
        checkAvailability(requestDto, overlappingBookings);
    }

    private Boolean isOverlapping(Booking booking, CreateBookingRequestDto requestDto) {
        return !(requestDto.checkInDate().isAfter(booking.getCheckOutDate())
                || requestDto.checkOutDate().isBefore(booking.getCheckInDate()));
    }

    private void checkAvailability(
            CreateBookingRequestDto requestDto, List<Booking> overlappingBookings) {
        if (!overlappingBookings.isEmpty()) {
            Accommodation accommodation = findAccommodationById(requestDto.accommodationId());
            boolean hasAvailability = accommodation.getAvailability() > overlappingBookings.size();
            if (!hasAvailability) {
                throw new DataProcessingException("No available accommodations left for booking.");
            }
        }
    }

    private Booking createBookingEntity(
            CreateBookingRequestDto requestDto, Accommodation accommodation, User user) {
        return bookingMapper.toEntity(requestDto)
                .setAccommodation(accommodation)
                .setUser(user)
                .setStatus(Booking.Status.PENDING);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + userId));
    }

    private Booking findBookingByUserAndId(Long userId, Long bookingId) {
        return bookingRepository.findByUserIdAndId(userId, bookingId).orElseThrow(() ->
                new EntityNotFoundException(
                        "No booking found with id: " + bookingId + " for user id: " + userId));
    }

    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Accommodation not found with id: " + id));
    }
}
