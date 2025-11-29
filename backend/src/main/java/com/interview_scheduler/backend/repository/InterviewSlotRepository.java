package com.interview_scheduler.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.interview_scheduler.backend.entity.InterviewSlot;
import com.interview_scheduler.backend.entity.SlotStatus;

@Repository
public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM InterviewSlot s WHERE s.id = :id")
        Optional<InterviewSlot> findByIdWithLock(@Param("id") Long id);

        List<InterviewSlot> findByInterviewerIdAndStatus(Long interviewerId, SlotStatus status);

        @Query("SELECT s FROM InterviewSlot s WHERE s.status = :status " +
                        "AND s.startTime >= :startDate AND s.startTime <= :endDate " +
                        "ORDER BY s.startTime ASC")
        List<InterviewSlot> findAvailableSlotsInDateRange(
                        @Param("status") SlotStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT s FROM InterviewSlot s WHERE s.status = :status " +
                        "AND s.startTime >= :startDate ORDER BY s.startTime ASC")
        Page<InterviewSlot> findAvailableSlotsPageable(
                        @Param("status") SlotStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        Pageable pageable);

        @Query("SELECT s FROM InterviewSlot s WHERE s.status = :status " +
                        "AND s.startTime >= :startDate AND s.id > :cursor " +
                        "ORDER BY s.id ASC")
        List<InterviewSlot> findAvailableSlotsByCursor(
                        @Param("status") SlotStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("cursor") Long cursor,
                        Pageable pageable);

        @Query("SELECT s FROM InterviewSlot s WHERE s.status = :status " +
                        "AND s.startTime > :cursorTime " +
                        "ORDER BY s.startTime ASC, s.id ASC")
        List<InterviewSlot> findAvailableSlotsByTimeCursor(
                        @Param("status") SlotStatus status,
                        @Param("cursorTime") LocalDateTime cursorTime,
                        Pageable pageable);

        @Query("SELECT COUNT(s) FROM InterviewSlot s WHERE s.interviewer.id = :interviewerId " +
                        "AND s.weekNumber = :weekNumber AND s.year = :year " +
                        "AND s.status IN (:statuses)")
        long countBookedSlotsForWeek(
                        @Param("interviewerId") Long interviewerId,
                        @Param("weekNumber") Integer weekNumber,
                        @Param("year") Integer year,
                        @Param("statuses") List<SlotStatus> statuses);

        List<InterviewSlot> findByWeekNumberAndYear(Integer weekNumber, Integer year);

        @Query("SELECT s FROM InterviewSlot s WHERE s.interviewer.id = :interviewerId " +
                        "AND s.startTime = :startTime")
        Optional<InterviewSlot> findByInterviewerIdAndStartTime(
                        @Param("interviewerId") Long interviewerId,
                        @Param("startTime") LocalDateTime startTime);

        List<InterviewSlot> findByInterviewerId(Long interviewerId);

        @Query("SELECT s FROM InterviewSlot s WHERE s.status = 'AVAILABLE' AND s.startTime < :now")
        List<InterviewSlot> findExpiredAvailableSlots(@Param("now") LocalDateTime now);

        @Query("UPDATE InterviewSlot s SET s.status = 'EXPIRED' WHERE s.status = 'AVAILABLE' AND s.startTime < :now")
        int markExpiredSlots(@Param("now") LocalDateTime now);
}
