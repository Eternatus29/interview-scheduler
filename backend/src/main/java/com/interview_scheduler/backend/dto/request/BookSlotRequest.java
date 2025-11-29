package com.interview_scheduler.backend.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSlotRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private String bookingNotes;
}
