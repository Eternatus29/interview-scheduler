package com.interview_scheduler.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class GeneratedSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime slotTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    private Long candidateId;

    @ManyToOne
    private InterviewerSlot interviewerSlot;
}
