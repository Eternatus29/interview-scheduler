package com.interview_scheduler.backend.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.interview_scheduler.backend.entity.WeeklyAvailability;

@Repository
public interface WeeklyAvailabilityRepository extends JpaRepository<WeeklyAvailability, Long> {

    List<WeeklyAvailability> findByInterviewerIdAndIsActiveTrue(Long interviewerId);

    List<WeeklyAvailability> findByInterviewerId(Long interviewerId);

    List<WeeklyAvailability> findByInterviewerIdAndDayOfWeekAndIsActiveTrue(
            Long interviewerId, DayOfWeek dayOfWeek);

    void deleteByInterviewerId(Long interviewerId);

    @Query("SELECT COUNT(w) > 0 FROM WeeklyAvailability w WHERE w.interviewer.id = :interviewerId AND w.isActive = true")
    boolean hasActiveAvailability(@Param("interviewerId") Long interviewerId);
}
