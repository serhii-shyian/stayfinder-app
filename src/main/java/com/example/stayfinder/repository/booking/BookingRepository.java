package com.example.stayfinder.repository.booking;

import com.example.stayfinder.model.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Optional<Booking> findByUserIdAndId(Long userId, Long bookingId);

    List<Booking> findByAccommodationId(Long id);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :bookingId")
    void updateStatus(@Param("bookingId") Long bookingId,
                      @Param("status") Booking.Status status);

    List<Booking> findByCheckOutDateAndStatusNot(LocalDateTime time,
                                                 Booking.Status status);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :bookingIds")
    void updateStatusForExpiredBooking(@Param("bookingIds") Set<Long> bookingIds,
                                       @Param("status") Booking.Status status);
}
