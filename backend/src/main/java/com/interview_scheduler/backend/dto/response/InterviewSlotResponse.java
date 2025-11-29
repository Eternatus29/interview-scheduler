package com.interview_scheduler.backend.dto.response;

import java.time.LocalDateTime;

import com.interview_scheduler.backend.entity.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSlotResponse {

    private Long id;
    private Long interviewerId;
    private String interviewerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SlotStatus status;
    private Integer weekNumber;
    private Integer year;
}
