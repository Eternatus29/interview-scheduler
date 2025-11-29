package com.interview_scheduler.backend.service;

import java.util.List;

import com.interview_scheduler.backend.dto.request.CreateInterviewerRequest;
import com.interview_scheduler.backend.dto.request.WeeklyAvailabilityRequest;
import com.interview_scheduler.backend.dto.response.InterviewerResponse;

public interface InterviewerService {

    InterviewerResponse createInterviewer(CreateInterviewerRequest request);

    InterviewerResponse getInterviewerById(Long id);

    List<InterviewerResponse> getAllInterviewers();

    InterviewerResponse updateWeeklyAvailability(Long interviewerId,
            List<WeeklyAvailabilityRequest> availabilities);

    InterviewerResponse updateMaxInterviewsPerWeek(Long interviewerId, Integer maxInterviews);

    void deleteInterviewer(Long id);
}
