package com.interview_scheduler.backend.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.interview_scheduler.backend.entity.Booking;
import com.interview_scheduler.backend.entity.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT b FROM Booking b WHERE b.id = :id")
        Optional<Booking> findByIdWithLock(@Param("id") Long id);

        Optional<Booking> findBySlotId(Long slotId);

        List<Booking> findByCandidateId(Long candidateId);

        @Query("SELECT b FROM Booking b WHERE b.candidate.id = :candidateId " +
                        "AND b.status NOT IN ('CANCELLED')")
        List<Booking> findActiveByCandidateId(@Param("candidateId") Long candidateId);

        @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.candidate.id = :candidateId " +
                        "AND b.status NOT IN ('CANCELLED') " +
                        "AND b.slot.startTime >= :startDate AND b.slot.startTime <= :endDate")
        boolean hasActiveBookingInDateRange(
                        @Param("candidateId") Long candidateId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        @Query("SELECT b FROM Booking b WHERE b.candidate.id = :candidateId " +
                        "AND b.status NOT IN ('CANCELLED') " +
                        "AND b.slot.startTime >= :startDate AND b.slot.startTime <= :endDate")
        Optional<Booking> findActiveBookingInDateRange(
                        @Param("candidateId") Long candidateId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        long countByWeekNumberAndYearAndStatus(Integer weekNumber, Integer year, BookingStatus status);

        List<Booking> findByStatus(BookingStatus status);

        boolean existsBySlotIdAndStatusNot(Long slotId, BookingStatus status);
}
