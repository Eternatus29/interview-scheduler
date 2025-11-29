package com.interview_scheduler.backend.service;

import java.util.List;

import com.interview_scheduler.backend.dto.request.CreateCandidateRequest;
import com.interview_scheduler.backend.dto.response.CandidateResponse;

public interface CandidateService {

    CandidateResponse createCandidate(CreateCandidateRequest request);

    CandidateResponse getCandidateById(Long id);

    CandidateResponse getCandidateByEmail(String email);

    List<CandidateResponse> getAllCandidates();

    void deleteCandidate(Long id);
}
