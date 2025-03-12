package com.example.stayfinder.controller;

import com.example.stayfinder.dto.payment.CreatePaymentSessionDto;
import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import com.example.stayfinder.service.payment.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment management", description = "Endpoint for managing payments")
public class PaymentController {
    private final StripePaymentService paymentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all payments by booking user id",
            description = "Getting a list of payments by booking user id")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentLowInfoDto> getAllByBookingUserId(
            @RequestParam(name = "user_id") Long userId, Pageable pageable) {
        return paymentService.findAllByBookingUserId(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a session by booking id",
            description = "Creating a stripe session by booking id")
    @PreAuthorize("hasRole('USER')")
    public PaymentDto createSession(@RequestBody CreatePaymentSessionDto requestDto) {
        return paymentService.createSession(requestDto.bookingId());
    }

    @GetMapping("/success")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Handle success payment",
            description = "Handling success executed payment")
    @PreAuthorize("hasRole('USER')")
    public PaymentWithoutSessionDto handleSuccessPayment(@RequestParam String sessionId) {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Handle cancelled payment",
            description = "Handling cancelled stripe payment")
    @PreAuthorize("hasRole('USER')")
    public String handleCancelledPayment(@RequestParam String sessionId) {
        return paymentService.processCancelPayment(sessionId);
    }
}
