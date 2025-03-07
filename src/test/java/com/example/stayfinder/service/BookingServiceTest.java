package com.example.stayfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.exception.DataProcessingException;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.BookingMapper;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.accommodation.AccommodationRepository;
import com.example.stayfinder.repository.booking.BookingRepository;
import com.example.stayfinder.repository.booking.BookingSpecificationBuilder;
import com.example.stayfinder.repository.user.UserRepository;
import com.example.stayfinder.service.booking.BookingServiceImpl;
import com.example.stayfinder.service.notification.NotificationService;
import com.example.stayfinder.service.payment.StripePaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingSpecificationBuilder specificationBuilder;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StripePaymentService paymentService;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("""
            Save booking when user has no unpaid reservations
            """)
    public void saveBooking_NoUnpaidReservations_ReturnsBookingDto() {
        // Given
        User user = createUser();
        Accommodation accommodation = createAccommodation();
        CreateBookingRequestDto requestDto = createBookingRequestDto();
        Booking booking = createBooking();
        BookingDto expected = createBookingDto(booking);

        when(paymentService.existsByBookingUserIdAndStatus(user.getId()))
                .thenReturn(false);
        when(accommodationRepository.findById(requestDto.accommodationId()))
                .thenReturn(Optional.of(accommodation));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingMapper.toEntity(requestDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        // When
        BookingDto actual = bookingService.save(user, requestDto);

        // Then
        assertEquals(expected, actual);
        verify(notificationService).sendCreateBookingMessage(
                accommodation, user, booking);
        verifyNoMoreInteractions(
                accommodationRepository, userRepository, bookingMapper);
    }

    @Test
    @DisplayName("""
            Save booking when user has unpaid reservations throws exception
            """)
    public void saveBooking_UserHasUnpaidReservations_ThrowsException() {
        // Given
        User user = createUser();
        CreateBookingRequestDto requestDto = createBookingRequestDto();

        when(paymentService.existsByBookingUserIdAndStatus(
                user.getId())).thenReturn(true);

        // Then
        assertThrows(DataProcessingException.class,
                () -> bookingService.save(user, requestDto));
    }

    @Test
    @DisplayName("""
            Find bookings by user id and status when bookings exist
            """)
    public void findBookings_ValidParameters_ReturnsBookingDtoList() {
        // Given
        BookingFilterParameters filter = createBookingFilterParameters();
        Booking booking = createBooking();
        BookingDto expected = createBookingDto(booking);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Booking> page = createPageImpl(booking);

        Specification<Booking> specification = specificationBuilder.build(filter);
        when(specificationBuilder.build(filter)).thenReturn(specification);
        when(bookingRepository.findAll(specification, pageable)).thenReturn(page);
        when(bookingMapper.toDtoList(page.getContent())).thenReturn(List.of(expected));

        // When
        List<BookingDto> actual = bookingService.findAllByUserIdAndStatus(filter, pageable);

        // Then
        assertEquals(List.of(expected), actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("""
            Find bookings by user id and status when filter parameters are invalid
            """)
    public void findBookings_InvalidFilterParameters_ThrowsException() {
        // Given
        BookingFilterParameters filter = new BookingFilterParameters(
                new String[]{"999"}, new String[]{"INVALID_STATUS"});
        Pageable pageable = PageRequest.of(0, 5);
        Page<Booking> emptyPage = Page.empty();

        Specification<Booking> specification = specificationBuilder.build(filter);
        when(specificationBuilder.build(filter)).thenReturn(specification);
        when(bookingRepository.findAll(specification, pageable)).thenReturn(emptyPage);

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.findAllByUserIdAndStatus(filter, pageable));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("""
            Find bookings by user id when bookings exist
            """)
    public void findBookingsByUserId_ValidUserId_ReturnsBookingDtoList() {
        // Given
        Long userId = 1L;
        Booking booking = createBooking();
        BookingDto expected = createBookingDto(booking);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Booking> page = createPageImpl(booking);

        when(bookingRepository.findByUserId(userId, pageable)).thenReturn(page);
        when(bookingMapper.toDtoList(page.getContent())).thenReturn(List.of(expected));

        // When
        List<BookingDto> actual = bookingService.findAllByUserId(userId, pageable);

        // Then
        assertEquals(List.of(expected), actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("""
            Find bookings by user id when no bookings exist for user
            """)
    public void findBookingsByUserId_NoBookingsForUserId_ThrowsException() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Booking> emptyPage = Page.empty();

        when(bookingRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.findAllByUserId(userId, pageable));
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("""
            Find booking by user id and booking id when booking exists
            """)
    public void findBookingByUserIdAndBookingId_ExistingBooking_ReturnsBookingDto() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        Booking booking = createBooking();
        BookingDto expected = createBookingDto(booking);

        when(bookingRepository.findByUserIdAndId(userId, bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        // When
        BookingDto actual = bookingService.findByUserIdAndId(userId, bookingId);

        // Then
        assertEquals(expected, actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("""
            Find booking by user id and booking id when booking does not exist
            """)
    public void findBookingByUserIdAndBookingId_NonExistingBooking_ThrowsException() {
        // Given
        Long userId = 1L;
        Long bookingId = 99L;

        when(bookingRepository.findByUserIdAndId(userId, bookingId))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.findByUserIdAndId(userId, bookingId));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("""
            Update booking by user id and booking id when booking exists
            """)
    public void updateByUserIdAndId_ExistingBooking_ReturnsBookingDto() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        CreateBookingRequestDto requestDto = createBookingRequestDto();
        Booking existingBooking = createBooking();
        Booking updatedBooking = createBooking()
                .setCheckInDate(LocalDateTime.now())
                .setCheckOutDate(LocalDateTime.now().plusMonths(1));;
        BookingDto expected = createBookingDto(updatedBooking);

        when(bookingRepository.findByUserIdAndId(userId, bookingId))
                .thenReturn(Optional.of(existingBooking));
        doNothing().when(bookingMapper).updateEntityFromDto(
                requestDto, existingBooking);
        when(bookingRepository.save(existingBooking)).thenReturn(updatedBooking);
        when(bookingMapper.toDto(updatedBooking)).thenReturn(expected);

        // When
        BookingDto actual = bookingService.updateByUserIdAndId(
                userId, bookingId, requestDto);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            Update booking by userId and bookingId when booking does not exist
            """)
    public void updateBooking_NonExistingBooking_ThrowsException() {
        // Given
        Long userId = 1L;
        Long bookingId = 100L;
        CreateBookingRequestDto requestDto = createBookingRequestDto();

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.updateByUserIdAndId(userId, bookingId, requestDto));
    }

    @Test
    @DisplayName("""
            Cancel booking by user id and booking id when booking exists and not canceled
            """)
    public void cancelByUserIdAndId_ExistingNotCanceledBooking_CancelsBookingAndSendsNotify() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        Booking existingBooking = createBooking();
        existingBooking.setStatus(Booking.Status.CONFIRMED);
        User user = createUser();

        when(bookingRepository.findByUserIdAndId(userId, bookingId))
                .thenReturn(Optional.of(existingBooking));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // When
        bookingService.cancelByUserIdAndId(userId, bookingId);

        // Then
        verify(bookingRepository).updateStatus(bookingId, Booking.Status.CANCELED);
        verify(notificationService).sendCancelBookingMessage(user, existingBooking);
        verifyNoMoreInteractions(bookingRepository, notificationService, userRepository);
    }

    @Test
    @DisplayName("""
            Cancel booking by user id and booking id when booking is already canceled
            """)
    public void cancelByUserIdAndId_ExistingAlreadyCanceledBooking_ThrowsException() {
        // Given
        Long userId = 1L;
        Long bookingId = 1L;
        Booking existingBooking = createBooking();
        existingBooking.setStatus(Booking.Status.CANCELED);

        when(bookingRepository.findByUserIdAndId(userId, bookingId))
                .thenReturn(Optional.of(existingBooking));

        // When & Then
        assertThrows(DataProcessingException.class,
                () -> bookingService.cancelByUserIdAndId(userId, bookingId));
        verifyNoMoreInteractions(
                bookingRepository, notificationService, userRepository);
    }

    @Test
    @DisplayName("""
            Check hourly expired bookings and notify when there are bookings to expire
            """)
    public void checkHourlyExpiredBookings_BookingsExist_UpdatesStatusAndSendsNotify() {
        // Given
        LocalDateTime now = LocalDateTime.now()
                .withMinute(0).withSecond(0).withNano(0);
        Set<Long> bookingIds = Set.of(1L, 2L, 3L);
        List<Booking> bookings = createBookingList();
        List<User> users = createUserList();

        when(bookingRepository.findByCheckOutDateAndStatusNot(
                now, Booking.Status.CANCELED))
                .thenReturn(bookings);
        when(userRepository.findAllById(bookingIds))
                .thenReturn(users);

        // When
        bookingService.checkHourlyExpiredBookings();

        // Then
        verify(bookingRepository).updateStatusForExpiredBooking(
                bookingIds, Booking.Status.EXPIRED);
        verify(notificationService).sendReleaseAccommodationMessage(bookingIds, users);
        verifyNoMoreInteractions(bookingRepository, userRepository, notificationService);
    }

    @Test
    @DisplayName("""
            Check hourly expired bookings when no bookings to expire
            """)
    public void checkHourlyExpiredBookings_NoBookingsToExpire_DoesNothing() {
        // Given
        Set<Long> bookingIds = Set.of(1L, 2L, 3L);
        LocalDateTime now = LocalDateTime.now()
                .withMinute(0).withSecond(0).withNano(0);
        List<User> users = createUserList();

        when(bookingRepository.findByCheckOutDateAndStatusNot(
                now, Booking.Status.CANCELED))
                .thenReturn(Collections.emptyList());

        // When
        bookingService.checkHourlyExpiredBookings();

        // Then
        verify(notificationService, never()).sendReleaseAccommodationMessage(bookingIds, users);
        verify(bookingRepository, never()).updateStatusForExpiredBooking(
                bookingIds, Booking.Status.EXPIRED);
    }

    private User createUser() {
        return new User()
                .setId(1L)
                .setUsername("testUser");
    }

    private List<User> createUserList() {
        return List.of(
                new User()
                        .setId(1L)
                        .setUsername("testUser1"),
                new User()
                        .setId(2L)
                        .setUsername("testUser2"),
                new User()
                        .setId(3L)
                        .setUsername("testUser3"));
    }

    private BookingFilterParameters createBookingFilterParameters() {
        return new BookingFilterParameters(
                new String[]{"1"},
                new String[]{"PENDING"});
    }

    private Page<Booking> createPageImpl(Booking booking) {
        return new PageImpl<>(List.of(booking));
    }

    private CreateBookingRequestDto createBookingRequestDto() {
        return new CreateBookingRequestDto(
                LocalDateTime.of(
                        2025, 3, 1, 14, 0),
                LocalDateTime.of(
                        2025, 3, 7, 11, 0),
                1L);
    }

    private Address createAddress() {
        return new Address()
                .setAddress("City Center");
    }

    private Accommodation createAccommodation() {
        return new Accommodation()
                .setId(1L)
                .setType(Accommodation.Type.HOUSE)
                .setLocation(createAddress())
                .setSize("Large")
                .setAmenities(Set.of(
                        Accommodation.Amenities.AIR_CONDITIONING,
                        Accommodation.Amenities.WIFI))
                .setDailyRate(BigDecimal.valueOf(150.0))
                .setAvailability(10);
    }

    private Booking createBooking() {
        return new Booking()
                .setId(1L)
                .setCheckInDate(LocalDateTime.of(
                        2025, 3, 1, 14, 0))
                .setCheckOutDate(LocalDateTime.of(
                        2025, 3, 7, 11, 0))
                .setStatus(Booking.Status.PENDING)
                .setAccommodation(createAccommodation())
                .setUser(createUser());
    }

    private List<Booking> createBookingList() {
        LocalDateTime now = LocalDateTime.now()
                .withMinute(0).withSecond(0).withNano(0);
        return List.of(
                new Booking()
                        .setId(1L)
                        .setCheckOutDate(now.minusHours(1))
                        .setStatus(Booking.Status.CANCELED),
                new Booking()
                        .setId(2L)
                        .setCheckOutDate(now.minusHours(1))
                        .setStatus(Booking.Status.CANCELED),
                new Booking()
                        .setId(3L)
                        .setCheckOutDate(now.minusHours(1))
                        .setStatus(Booking.Status.CANCELED)
        );
    }

    private BookingDto createBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getAccommodation().getId(),
                booking.getUser().getUsername(),
                booking.getStatus().name());
    }
}
