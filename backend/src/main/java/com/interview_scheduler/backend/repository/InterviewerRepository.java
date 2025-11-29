package com.interview_scheduler.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.interview_scheduler.backend.entity.Interviewer;

public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
}
