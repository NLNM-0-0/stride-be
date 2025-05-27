package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.coreservice.model.Goal;
import com.stride.tracking.coreservice.model.GoalHistory;
import com.stride.tracking.coreservice.repository.GoalHistoryRepository;
import com.stride.tracking.coreservice.repository.GoalRepository;
import com.stride.tracking.coreservice.utils.GoalTimeFrameHelper;
import com.stride.tracking.dto.fcm.request.PushFCMNotificationRequest;
import com.stride.tracking.dto.goal.GoalTimeFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalTaskScheduler {
    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;

    private final KafkaProducer kafkaProducer;

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    @Scheduled(cron = "0 0 0 1 1 ?")
    @Transactional
    public void runGoalHistoryDeletionTask() {
        LocalDate janFirst = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        Instant startInstant = janFirst.atStartOfDay(ZoneId.systemDefault()).toInstant();
        goalHistoryRepository.deleteAllByCreatedAtBefore(startInstant);
    }

    @Scheduled(cron = "0 0 0 ? * 6")
    @Transactional
    public void runGoalWeeklyReminderTask() {
        sendGoalReminder(GoalTimeFrame.WEEKLY);
    }

    private void sendGoalReminder(GoalTimeFrame timeFrame) {
        List<Goal> goals = goalRepository.findByTimeFrame(timeFrame);

        for (Goal goal : goals) {
            Long target = goal.getAmount();

            Long progress = 0L;
            Optional<GoalHistory> goalHistory = getCurrentGoalHistory(goal);
            if (goalHistory.isPresent()) {
                progress = goalHistory.get().getAmountGain();
            }

            if (progress >= target) {
                return;
            }

            sendGoalReminderNotification(
                    goal.getUserId(),
                    goal,
                    progress
            );
        }
    }

    private Optional<GoalHistory> getCurrentGoalHistory(Goal goal) {
        Calendar calendar = GoalTimeFrameHelper.getCalendar(goal.getTimeFrame(), ZONE_ID);
        String key = GoalTimeFrameHelper.formatDateKey(calendar.getTime(), goal.getTimeFrame());

        return goalHistoryRepository.findByGoalIdAndDateKey(goal.getId(), key);
    }

    private void sendGoalReminderNotification(String userId, Goal goal, Long progress) {
        String message = String.format(
                "You have a goal for %s (%s) %s: target %d, progress %d, left %d",
                goal.getSport().getName(),
                goal.getType(),
                goal.getTimeFrame(),
                goal.getAmount(),
                progress,
                goal.getAmount() - progress
        );

        PushFCMNotificationRequest request = PushFCMNotificationRequest.builder()
                .userId(userId)
                .title("Goal Reminder")
                .message(message)
                .build();

        kafkaProducer.send(KafkaTopics.FCM_TOPIC, request);
    }

    @Scheduled(cron = "0 0 0 1 12 ?")
    public void runGoalMonthlyReminderTask() {
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(today.getYear(), today.getMonth());
        LocalDate targetDate = yearMonth.atEndOfMonth().minusDays(7);

        // Check whether today is exactly 7 days before the end of the current month
        if (today.equals(targetDate)) {
            sendGoalReminder(GoalTimeFrame.MONTHLY);
        }
    }

    @Scheduled(cron = "0 0 0 1 12 ?")
    public void runGoalAnnuallyReminderTask() {
        sendGoalReminder(GoalTimeFrame.ANNUALLY);
    }
}
