package com.interview_scheduler.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "interviewers", indexes = {
        @Index(name = "idx_interviewer_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Valid email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @Min(value = 1, message = "Max interviews per week must be at least 1")
    @Column(name = "max_interviews_per_week", nullable = false)
    private Integer maxInterviewsPerWeek;

    @Column(name = "slot_duration_minutes", nullable = false)
    @Builder.Default
    private Integer slotDurationMinutes = 60;

    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<WeeklyAvailability> weeklyAvailabilities = new ArrayList<>();

    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<InterviewSlot> interviewSlots = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void addWeeklyAvailability(WeeklyAvailability availability) {
        weeklyAvailabilities.add(availability);
        availability.setInterviewer(this);
    }

    public void removeWeeklyAvailability(WeeklyAvailability availability) {
        weeklyAvailabilities.remove(availability);
        availability.setInterviewer(null);
    }
}
