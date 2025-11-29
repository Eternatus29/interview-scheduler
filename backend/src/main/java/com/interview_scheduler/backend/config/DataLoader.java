package com.interview_scheduler.backend.config;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.interview_scheduler.backend.entity.Candidate;
import com.interview_scheduler.backend.entity.Interviewer;
import com.interview_scheduler.backend.entity.WeeklyAvailability;
import com.interview_scheduler.backend.repository.CandidateRepository;
import com.interview_scheduler.backend.repository.InterviewerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

        private final InterviewerRepository interviewerRepository;
        private final CandidateRepository candidateRepository;

        @Override
        public void run(String... args) throws Exception {
                if (interviewerRepository.count() > 0 || candidateRepository.count() > 0) {
                        log.info("DataLoader: DB already contains data, skipping seeding.");
                        return;
                }

                log.info("DataLoader: Seeding sample data...");

                Interviewer interviewer1 = Interviewer.builder()
                                .name("John Smith")
                                .email("john.smith@company.com")
                                .maxInterviewsPerWeek(10)
                                .slotDurationMinutes(60)
                                .createdAt(LocalDateTime.now())
                                .build();

                for (DayOfWeek day : new DayOfWeek[] { DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY }) {
                        WeeklyAvailability availability = WeeklyAvailability.builder()
                                        .dayOfWeek(day)
                                        .startTime(LocalTime.of(9, 0))
                                        .endTime(LocalTime.of(17, 0))
                                        .isActive(true)
                                        .build();
                        interviewer1.addWeeklyAvailability(availability);
                }

                interviewerRepository.save(interviewer1);

                Interviewer interviewer2 = Interviewer.builder()
                                .name("Sarah Johnson")
                                .email("sarah.johnson@company.com")
                                .maxInterviewsPerWeek(8)
                                .slotDurationMinutes(45)
                                .createdAt(LocalDateTime.now())
                                .build();

                for (DayOfWeek day : new DayOfWeek[] { DayOfWeek.TUESDAY, DayOfWeek.THURSDAY }) {
                        WeeklyAvailability availability = WeeklyAvailability.builder()
                                        .dayOfWeek(day)
                                        .startTime(LocalTime.of(10, 0))
                                        .endTime(LocalTime.of(16, 0))
                                        .isActive(true)
                                        .build();
                        interviewer2.addWeeklyAvailability(availability);
                }

                interviewerRepository.save(interviewer2);

                Candidate candidate1 = Candidate.builder()
                                .name("Alice Brown")
                                .email("alice.brown@email.com")
                                .phoneNumber("+1-555-0101")
                                .createdAt(LocalDateTime.now())
                                .build();
                candidateRepository.save(candidate1);

                Candidate candidate2 = Candidate.builder()
                                .name("Bob Wilson")
                                .email("bob.wilson@email.com")
                                .phoneNumber("+1-555-0102")
                                .createdAt(LocalDateTime.now())
                                .build();
                candidateRepository.save(candidate2);

                Candidate candidate3 = Candidate.builder()
                                .name("Carol Davis")
                                .email("carol.davis@email.com")
                                .phoneNumber("+1-555-0103")
                                .createdAt(LocalDateTime.now())
                                .build();
                candidateRepository.save(candidate3);

                log.info("DataLoader: Seeded 2 interviewers and 3 candidates successfully.");
                log.info("DataLoader: Use POST /api/slots/generate to generate interview slots.");
        }
}
