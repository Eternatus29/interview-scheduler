package com.interview_scheduler.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.interview_scheduler.backend.entity.InterviewerSlot;

public interface InterviewerSlotRepository extends JpaRepository<InterviewerSlot, Long> {
    List<InterviewerSlot> findByInterviewerId(Long interviewerId);
}
