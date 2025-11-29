package com.interview_scheduler.backend.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInterviewerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format is required")
    private String email;

    @NotNull(message = "Maximum interviews per week is required")
    @Min(value = 1, message = "Maximum interviews per week must be at least 1")
    private Integer maxInterviewsPerWeek;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Builder.Default
    private Integer slotDurationMinutes = 60;
    
    @Valid
    private List<WeeklyAvailabilityRequest> weeklyAvailabilities;
}
