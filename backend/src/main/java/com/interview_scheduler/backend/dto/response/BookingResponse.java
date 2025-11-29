package com.interview_scheduler.backend.dto.response;

import java.time.LocalDateTime;

import com.interview_scheduler.backend.entity.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long slotId;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private Long interviewerId;
    private String interviewerName;
    private BookingStatus status;
    private String bookingNotes;
    private Integer weekNumber;
    private Integer year;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}
