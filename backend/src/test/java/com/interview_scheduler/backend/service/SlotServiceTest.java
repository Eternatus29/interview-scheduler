package com.interview_scheduler.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.interview_scheduler.backend.dto.request.GenerateSlotsRequest;
import com.interview_scheduler.backend.dto.response.InterviewSlotResponse;
import com.interview_scheduler.backend.dto.response.PaginatedResponse;
import com.interview_scheduler.backend.entity.Interviewer;
import com.interview_scheduler.backend.entity.InterviewSlot;
import com.interview_scheduler.backend.entity.SlotStatus;
import com.interview_scheduler.backend.entity.WeeklyAvailability;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.ValidationException;
import com.interview_scheduler.backend.repository.InterviewSlotRepository;
import com.interview_scheduler.backend.repository.InterviewerRepository;
import com.interview_scheduler.backend.repository.WeeklyAvailabilityRepository;
import com.interview_scheduler.backend.service.impl.SlotServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Slot Service Tests")
class SlotServiceTest {

        @Mock
        private InterviewSlotRepository slotRepository;

        @Mock
        private InterviewerRepository interviewerRepository;

        @Mock
        private WeeklyAvailabilityRepository weeklyAvailabilityRepository;

        @InjectMocks
        private SlotServiceImpl slotService;

        private Interviewer testInterviewer;
        private List<WeeklyAvailability> testAvailabilities;

        @BeforeEach
        void setUp() {
                testInterviewer = Interviewer.builder()
                                .id(1L)
                                .name("Test Interviewer")
                                .email("interviewer@test.com")
                                .maxInterviewsPerWeek(10)
                                .slotDurationMinutes(60)
                                .build();

                WeeklyAvailability mondayAvailability = WeeklyAvailability.builder()
                                .id(1L)
                                .interviewer(testInterviewer)
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(17, 0))
                                .isActive(true)
                                .build();

                WeeklyAvailability wednesdayAvailability = WeeklyAvailability.builder()
                                .id(2L)
                                .interviewer(testInterviewer)
                                .dayOfWeek(DayOfWeek.WEDNESDAY)
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(16, 0))
                                .isActive(true)
                                .build();

                testAvailabilities = Arrays.asList(mondayAvailability, wednesdayAvailability);
        }

        @Nested
        @DisplayName("Generate Slots Tests")
        class GenerateSlotsTests {

                @Test
                @DisplayName("Should generate slots based on weekly availability")
                void generateSlots_Success() {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(1L)
                                        .weeksToGenerate(2)
                                        .build();

                        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
                        when(weeklyAvailabilityRepository.findByInterviewerIdAndIsActiveTrue(1L))
                                        .thenReturn(testAvailabilities);
                        when(slotRepository.findByInterviewerIdAndStartTime(anyLong(), any()))
                                        .thenReturn(Optional.empty());
                        when(slotRepository.save(any(InterviewSlot.class))).thenAnswer(i -> {
                                InterviewSlot slot = i.getArgument(0);
                                slot.setId(1L);
                                return slot;
                        });

                        List<InterviewSlotResponse> result = slotService.generateSlots(request);

                        assertNotNull(result);
                        assertTrue(result.size() > 0, "Should generate at least one slot");
                        verify(slotRepository, atLeastOnce()).save(any(InterviewSlot.class));
                }

                @Test
                @DisplayName("Should throw ResourceNotFoundException for non-existent interviewer")
                void generateSlots_InterviewerNotFound() {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(999L)
                                        .build();

                        when(interviewerRepository.findById(999L)).thenReturn(Optional.empty());

                        assertThrows(ResourceNotFoundException.class, () -> slotService.generateSlots(request));
                }

                @Test
                @DisplayName("Should throw ValidationException when no availability defined")
                void generateSlots_NoAvailability() {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(1L)
                                        .build();

                        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
                        when(weeklyAvailabilityRepository.findByInterviewerIdAndIsActiveTrue(1L))
                                        .thenReturn(List.of());

                        assertThrows(ValidationException.class, () -> slotService.generateSlots(request));
                }

                @Test
                @DisplayName("Should skip duplicate slots during generation")
                void generateSlots_SkipsDuplicates() {
                        GenerateSlotsRequest request = GenerateSlotsRequest.builder()
                                        .interviewerId(1L)
                                        .weeksToGenerate(1)
                                        .build();

                        InterviewSlot existingSlot = InterviewSlot.builder()
                                        .id(1L)
                                        .interviewer(testInterviewer)
                                        .status(SlotStatus.AVAILABLE)
                                        .build();

                        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
                        when(weeklyAvailabilityRepository.findByInterviewerIdAndIsActiveTrue(1L))
                                        .thenReturn(testAvailabilities);
                        when(slotRepository.findByInterviewerIdAndStartTime(anyLong(), any()))
                                        .thenReturn(Optional.of(existingSlot));

                        slotService.generateSlots(request);

                        verify(slotRepository, never()).save(any(InterviewSlot.class));
                }
        }

        @Nested
        @DisplayName("Get Available Slots Tests")
        class GetAvailableSlotsTests {

                @Test
                @DisplayName("Should return paginated available slots (offset-based)")
                void getAvailableSlots_OffsetPagination() {
                        InterviewSlot slot1 = InterviewSlot.builder()
                                        .id(1L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().plusDays(1))
                                        .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .weekNumber(1)
                                        .year(2025)
                                        .build();

                        Page<InterviewSlot> slotsPage = new PageImpl<>(List.of(slot1));
                        when(slotRepository.findAvailableSlotsPageable(any(), any(), any(Pageable.class)))
                                        .thenReturn(slotsPage);

                        PaginatedResponse<InterviewSlotResponse> result = slotService.getAvailableSlots(0, 10);

                        assertNotNull(result);
                        assertEquals(1, result.getData().size());
                        assertEquals(0, result.getPage());
                        assertEquals(10, result.getSize());
                }

                @Test
                @DisplayName("Should return cursor-based paginated available slots")
                void getAvailableSlots_CursorPagination() {
                        InterviewSlot slot1 = InterviewSlot.builder()
                                        .id(5L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().plusDays(1))
                                        .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .weekNumber(1)
                                        .year(2025)
                                        .build();

                        InterviewSlot slot2 = InterviewSlot.builder()
                                        .id(6L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().plusDays(2))
                                        .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .weekNumber(1)
                                        .year(2025)
                                        .build();

                        when(slotRepository.findAvailableSlotsByCursor(any(), any(), anyLong(), any(Pageable.class)))
                                        .thenReturn(Arrays.asList(slot1, slot2));

                        PaginatedResponse<InterviewSlotResponse> result = slotService.getAvailableSlotsByCursor(0L, 10);

                        assertNotNull(result);
                        assertEquals(2, result.getData().size());
                        assertEquals(6L, result.getNextCursor());
                        assertFalse(result.getHasNext());
                }

                @Test
                @DisplayName("Should indicate hasNext when more results available")
                void getAvailableSlots_HasNext() {
                        List<InterviewSlot> slots = Arrays.asList(
                                        createSlot(1L), createSlot(2L), createSlot(3L));

                        when(slotRepository.findAvailableSlotsByCursor(any(), any(), anyLong(), any(Pageable.class)))
                                        .thenReturn(slots);

                        PaginatedResponse<InterviewSlotResponse> result = slotService.getAvailableSlotsByCursor(0L, 2);

                        assertEquals(2, result.getData().size());
                        assertTrue(result.getHasNext());
                }

                private InterviewSlot createSlot(Long id) {
                        return InterviewSlot.builder()
                                        .id(id)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().plusDays(id.intValue()))
                                        .endTime(LocalDateTime.now().plusDays(id.intValue()).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .weekNumber(1)
                                        .year(2025)
                                        .build();
                }
        }

        @Nested
        @DisplayName("Get Slot By ID Tests")
        class GetSlotByIdTests {

                @Test
                @DisplayName("Should return slot when found")
                void getSlotById_Success() {
                        InterviewSlot slot = InterviewSlot.builder()
                                        .id(1L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().plusDays(1))
                                        .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .weekNumber(1)
                                        .year(2025)
                                        .build();

                        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

                        InterviewSlotResponse result = slotService.getSlotById(1L);

                        assertNotNull(result);
                        assertEquals(1L, result.getId());
                }

                @Test
                @DisplayName("Should throw ResourceNotFoundException when slot not found")
                void getSlotById_NotFound() {
                        when(slotRepository.findById(999L)).thenReturn(Optional.empty());

                        assertThrows(ResourceNotFoundException.class, () -> slotService.getSlotById(999L));
                }
        }

        @Nested
        @DisplayName("Mark Expired Slots Tests")
        class MarkExpiredSlotsTests {

                @Test
                @DisplayName("Should mark expired available slots")
                void markExpiredSlots_Success() {
                        InterviewSlot expiredSlot1 = InterviewSlot.builder()
                                        .id(1L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().minusHours(2))
                                        .endTime(LocalDateTime.now().minusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .build();

                        InterviewSlot expiredSlot2 = InterviewSlot.builder()
                                        .id(2L)
                                        .interviewer(testInterviewer)
                                        .startTime(LocalDateTime.now().minusDays(1))
                                        .endTime(LocalDateTime.now().minusDays(1).plusHours(1))
                                        .status(SlotStatus.AVAILABLE)
                                        .build();

                        when(slotRepository.findExpiredAvailableSlots(any()))
                                        .thenReturn(Arrays.asList(expiredSlot1, expiredSlot2));
                        when(slotRepository.save(any(InterviewSlot.class))).thenAnswer(i -> i.getArgument(0));

                        int result = slotService.markExpiredSlots();

                        assertEquals(2, result);
                        verify(slotRepository, times(2)).save(any(InterviewSlot.class));
                }

                @Test
                @DisplayName("Should return 0 when no expired slots")
                void markExpiredSlots_NoExpired() {
                        when(slotRepository.findExpiredAvailableSlots(any())).thenReturn(List.of());

                        int result = slotService.markExpiredSlots();

                        assertEquals(0, result);
                        verify(slotRepository, never()).save(any(InterviewSlot.class));
                }
        }
}
