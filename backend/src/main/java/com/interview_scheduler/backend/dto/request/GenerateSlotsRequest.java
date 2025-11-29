package com.interview_scheduler.backend.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSlotsRequest {

    @NotNull(message = "Interviewer ID is required")
    private Long interviewerId;

    @Valid
    private List<WeeklyAvailabilityRequest> weeklyAvailabilities;
    @Builder.Default
    private Integer weeksToGenerate = 2;
}
