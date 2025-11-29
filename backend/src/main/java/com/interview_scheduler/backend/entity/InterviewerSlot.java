package com.interview_scheduler.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class InterviewerSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long interviewerId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isBooked;
    private int weekNumber;
}
