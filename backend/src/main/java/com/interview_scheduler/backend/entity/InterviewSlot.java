package com.interview_scheduler.backend.entity;

import java.time.LocalDateTime;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "interview_slots", indexes = {
        @Index(name = "idx_slot_interviewer", columnList = "interviewer_id"),
        @Index(name = "idx_slot_start_time", columnList = "start_time"),
        @Index(name = "idx_slot_status", columnList = "status"),
        @Index(name = "idx_slot_week_number", columnList = "week_number"),
        @Index(name = "idx_slot_cursor", columnList = "id, status, start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    @ToString.Exclude
    private Interviewer interviewer;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @OneToOne(mappedBy = "slot", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Booking booking;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    public boolean isPast() {
        return startTime.isBefore(LocalDateTime.now());
    }

    public void markAsBooked() {
        this.status = SlotStatus.BOOKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsAvailable() {
        this.status = SlotStatus.AVAILABLE;
        this.booking = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsConfirmed() {
        this.status = SlotStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
}
