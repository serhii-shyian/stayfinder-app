package com.example.stayfinder.service.payment;

import com.example.stayfinder.config.StripeConfig;
import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import com.example.stayfinder.exception.DataProcessingException;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.PaymentMapper;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.Payment;
import com.example.stayfinder.repository.booking.BookingRepository;
import com.example.stayfinder.repository.payment.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StripePaymentService implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;
    private final StripeConfig stripeConfig;

    @Override
    public List<PaymentLowInfoDto> findAllByBookingUserId(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByBookingUserId(userId, pageable);
        if (payments.isEmpty()) {
            throw new EntityNotFoundException("No payments found for user id: " + userId);
        }
        return paymentMapper.toDtoList(payments.getContent());
    }

    @Override
    public PaymentDto createSession(Long bookingId) {
        try {
            Booking booking = getBookingById(bookingId);
            BigDecimal totalAmount = calculateTotalAmount(booking);

            SessionCreateParams params = stripeConfig.createSessionParams(totalAmount);
            Session session = Session.create(params);

            Payment payment = savePayment(booking, session, totalAmount);
            return paymentMapper.toDto(payment);
        } catch (StripeException e) {
            throw new DataProcessingException("Error occurred while creating payment session", e);
        }
    }

    @Override
    public PaymentWithoutSessionDto processSuccessfulPayment(String sessionId) {
        Payment payment = findPaymentBySessionId(sessionId);
        updatePaymentStatus(payment, Payment.PaymentStatus.PAID);
        updateBookingStatus(payment.getBooking().getId(), Booking.Status.CONFIRMED);

        return paymentMapper.toWithoutSessionDto(payment);
    }

    @Override
    public String processCancelPayment(String sessionId) {
        updatePaymentStatus(findPaymentBySessionId(sessionId), Payment.PaymentStatus.PENDING);
        updateBookingStatus(findPaymentBySessionId(sessionId).getId(), Booking.Status.PENDING);

        return String.format("The payment for session ID '%s' "
                + "has been canceled and can be made later.", sessionId);
    }

    @Override
    public boolean existsByBookingUserIdAndStatus(Long userId) {
        return paymentRepository.existsByBookingUserIdAndStatus(
                userId, Payment.PaymentStatus.PENDING);
    }

    private Payment findPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found by session id: " + sessionId));
    }

    private Payment savePayment(Booking booking, Session session, BigDecimal totalAmount) {
        Payment payment = new Payment()
                .setBooking(booking)
                .setSessionId(session.getId())
                .setSessionUrl(session.getUrl())
                .setExpiredTime(session.getExpiresAt())
                .setAmount(totalAmount)
                .setStatus(Payment.PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException("Booking not found by id: " + bookingId));
    }

    private BigDecimal calculateTotalAmount(Booking booking) {
        long days = ChronoUnit.DAYS.between(
                booking.getCheckInDate().toLocalDate(),
                booking.getCheckOutDate().toLocalDate());
        return booking.getAccommodation().getDailyRate().multiply(BigDecimal.valueOf(days));
    }

    private void updatePaymentStatus(Payment payment, Payment.PaymentStatus status) {
        payment.setStatus(status);
        paymentRepository.updateStatus(payment.getId(), status);
    }

    private void updateBookingStatus(Long bookingId, Booking.Status status) {
        bookingRepository.updateStatus(bookingId, status);
    }
}
