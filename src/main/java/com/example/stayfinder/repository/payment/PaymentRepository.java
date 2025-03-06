package com.example.stayfinder.repository.payment;

import com.example.stayfinder.model.Payment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = {"booking"})
    Optional<Payment> findBySessionId(String sessionId);

    Page<Payment> findByBookingUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id IN :paymentId")
    void updateStatus(@Param("paymentId") Long paymentId,
                      @Param("status") Payment.PaymentStatus status);

    boolean existsByBookingUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.status = :pendingStatus "
            + "AND p.expiredTime < :currentTimestamp")
    void updateExpiredPayments(@Param("currentTimestamp") Long currentTimestamp,
                               @Param("status") Payment.PaymentStatus status,
                               @Param("pendingStatus") Payment.PaymentStatus pendingStatus);
}
