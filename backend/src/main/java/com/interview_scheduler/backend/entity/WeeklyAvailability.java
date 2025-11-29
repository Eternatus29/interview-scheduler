package com.interview_scheduler.backend.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "weekly_availabilities", indexes = {
        @Index(name = "idx_weekly_availability_interviewer", columnList = "interviewer_id"),
        @Index(name = "idx_weekly_availability_day", columnList = "day_of_week")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    @ToString.Exclude
    private Interviewer interviewer;

    @NotNull(message = "Day of week is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public boolean isValidTimeRange() {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }

    public int calculateNumberOfSlots(int slotDurationMinutes) {
        if (!isValidTimeRange() || slotDurationMinutes <= 0) {
            return 0;
        }
        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return (int) (totalMinutes / slotDurationMinutes);
    }
}
