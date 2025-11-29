package com.interview_scheduler.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interview_scheduler.backend.dto.request.CreateCandidateRequest;
import com.interview_scheduler.backend.dto.response.CandidateResponse;
import com.interview_scheduler.backend.entity.Candidate;
import com.interview_scheduler.backend.exception.ResourceNotFoundException;
import com.interview_scheduler.backend.exception.ValidationException;
import com.interview_scheduler.backend.repository.CandidateRepository;
import com.interview_scheduler.backend.service.CandidateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;

    @Override
    @Transactional
    public CandidateResponse createCandidate(CreateCandidateRequest request) {
        log.info("Creating candidate with email: {}", request.getEmail());

        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "A candidate with this email already exists");
        }

        Candidate candidate = Candidate.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .build();

        candidate = candidateRepository.save(candidate);

        log.info("Created candidate with ID: {}", candidate.getId());
        return mapToResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
        return mapToResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateByEmail(String email) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "email", email));
        return mapToResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateResponse> getAllCandidates() {
        return candidateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCandidate(Long id) {
        log.info("Deleting candidate: {}", id);

        if (!candidateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }

        candidateRepository.deleteById(id);
        log.info("Deleted candidate: {}", id);
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .createdAt(candidate.getCreatedAt())
                .build();
    }
}
