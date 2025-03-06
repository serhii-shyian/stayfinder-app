package com.example.stayfinder.service.booking;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto save(User user, CreateBookingRequestDto requestDto);

    List<BookingDto> findAllByUserIdAndStatus(
            BookingFilterParameters parameters, Pageable pageable);

    List<BookingDto> findAllByUserId(Long userId, Pageable pageable);

    BookingDto findByUserIdAndId(Long userId, Long bookingId);

    BookingDto updateByUserIdAndId(
            Long userId, Long bookingId, CreateBookingRequestDto requestDto);

    void cancelByUserIdAndId(Long userId, Long bookingId);
}
