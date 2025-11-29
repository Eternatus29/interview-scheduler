package com.interview_scheduler.backend.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.interview_scheduler.backend.entity.GeneratedSlot;
import com.interview_scheduler.backend.entity.SlotStatus;

public interface GeneratedSlotRepository extends JpaRepository<GeneratedSlot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GeneratedSlot> findById(Long id);

    Page<GeneratedSlot> findAll(Pageable pageable);

    List<GeneratedSlot> findByStatus(SlotStatus status);
}
