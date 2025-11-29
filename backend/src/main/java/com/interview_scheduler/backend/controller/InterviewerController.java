package com.interview_scheduler.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview_scheduler.backend.entity.Interviewer;
import com.interview_scheduler.backend.repository.InterviewerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interviewers")
@RequiredArgsConstructor
public class InterviewerController {

    private final InterviewerRepository interviewerRepo;

    @PostMapping
    public Interviewer addInterviewer(@RequestBody Interviewer interviewer) {
        return interviewerRepo.save(interviewer);
    }

    @GetMapping
    public List<Interviewer> list() {
        return interviewerRepo.findAll();
    }
}
