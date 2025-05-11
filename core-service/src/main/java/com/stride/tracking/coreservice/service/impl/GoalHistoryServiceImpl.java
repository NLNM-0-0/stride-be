package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.coreservice.repository.GoalHistoryRepository;
import com.stride.tracking.coreservice.service.GoalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GoalHistoryServiceImpl implements GoalHistoryService {
    private final GoalHistoryRepository goalHistoryRepository;

    @Scheduled(cron = "0 0 0 1 1 ?")
    @Override
    @Transactional
    public void runGoalHistoryDeletionTask() {
        LocalDate janFirst = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        Instant startInstant = janFirst.atStartOfDay(ZoneId.systemDefault()).toInstant();
        goalHistoryRepository.deleteAllByCreatedAtBefore(startInstant);
    }

}
