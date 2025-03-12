package com.example.stayfinder.service.booking;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto save(User user, CreateBookingRequestDto requestDto);

    Page<BookingDto> findAllByUserIdAndStatus(
            BookingFilterParameters parameters, Pageable pageable);

    Page<BookingDto> findAllByUserId(Long userId, Pageable pageable);

    BookingDto findByUserIdAndId(Long userId, Long bookingId);

    BookingDto updateByUserIdAndId(
            Long userId, Long bookingId, CreateBookingRequestDto requestDto);

    void cancelByUserIdAndId(Long userId, Long bookingId);

    void checkHourlyExpiredBookings();
}
