package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.metricservice.repository.ActivityMetricRepository;
import com.stride.tracking.metricservice.repository.UserMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class CleanDataScheduler {
    private final ActivityMetricRepository activityMetricRepository;
    private final UserMetricRepository userMetricRepository;

    @Scheduled(cron = "0 0 0 1 1 *")
    public void cleanOldData() {
        Instant oneYearAgo = Instant.now()
                .minus(1, ChronoUnit.YEARS)
                .minus(1, ChronoUnit.DAYS);

        log.info("Start cleaning data older than {}", oneYearAgo);

        activityMetricRepository.deleteByTimeBefore(oneYearAgo);
        userMetricRepository.deleteByTimeBefore(oneYearAgo);
    }
}
