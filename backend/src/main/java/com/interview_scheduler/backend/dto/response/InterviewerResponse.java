package com.interview_scheduler.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerResponse {

    private Long id;
    private String name;
    private String email;
    private Integer maxInterviewsPerWeek;
    private Integer slotDurationMinutes;
    private List<WeeklyAvailabilityResponse> weeklyAvailabilities;
    private LocalDateTime createdAt;
}
