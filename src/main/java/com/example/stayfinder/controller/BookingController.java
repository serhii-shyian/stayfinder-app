package com.example.stayfinder.controller;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.BookingFilterParameters;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.model.User;
import com.example.stayfinder.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Tag(name = "Booking management", description = "Endpoint for managing bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new booking",
            description = "Creating a new booking according to the parameters")
    @PreAuthorize("hasRole('USER')")
    public BookingDto create(Authentication authentication,
                             @RequestBody CreateBookingRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return bookingService.save(user, requestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a bookings by user id or status",
            description = "Getting a list of bookings by user id or status")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingDto> getAllByUserIdAndStatus(BookingFilterParameters parameters,
                                                    Pageable pageable) {
        return bookingService.findAllByUserIdAndStatus(parameters, pageable);
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all bookings for current user",
            description = "Getting all bookings for current user by user id")
    @PreAuthorize("hasRole('USER')")
    public List<BookingDto> getAllByAuthUserId(Authentication authentication,
                                               Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return bookingService.findAllByUserId(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get booking by id for current user",
            description = "Getting booking for current user by id and user id")
    @PreAuthorize("hasRole('USER')")
    public BookingDto getByAuthUserIdAndId(Authentication authentication,
                                           @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return bookingService.findByUserIdAndId(user.getId(), id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update booking by id for current user",
            description = "Updating booking for current user by id and user id")
    @PreAuthorize("hasRole('USER')")
    public BookingDto updateByAuthUserIdAndId(Authentication authentication,
                                              @PathVariable Long id,
                                              @RequestBody CreateBookingRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return bookingService.updateByUserIdAndId(user.getId(), id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel booking by id for current user",
            description = "Canceling booking for current user by id and user id")
    @PreAuthorize("hasRole('USER')")
    public void cancelByAuthUserIdAndId(Authentication authentication,
                                        @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        bookingService.cancelByUserIdAndId(user.getId(), id);
    }
}
