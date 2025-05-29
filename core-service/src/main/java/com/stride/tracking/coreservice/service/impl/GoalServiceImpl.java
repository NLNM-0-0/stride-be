package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.SecurityUtils;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.mapper.GoalMapper;
import com.stride.tracking.coreservice.model.Goal;
import com.stride.tracking.coreservice.model.GoalHistory;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.GoalHistoryRepository;
import com.stride.tracking.coreservice.repository.GoalRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
import com.stride.tracking.coreservice.service.GoalService;
import com.stride.tracking.coreservice.utils.GoalTimeFrameHelper;
import com.stride.tracking.dto.goal.request.CreateGoalRequest;
import com.stride.tracking.dto.goal.request.UpdateGoalRequest;
import com.stride.tracking.dto.goal.response.CreateGoalResponse;
import com.stride.tracking.dto.goal.response.GoalHistoryResponse;
import com.stride.tracking.dto.goal.response.GoalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;

    private final SportRepository sportRepository;

    private final GoalMapper goalMapper;

    @Override
    @Transactional(readOnly = true)
    public SimpleListResponse<GoalResponse> getUserGoals(ZoneId zoneId) {
        String userId = SecurityUtils.getCurrentUserId();

        List<Goal> goals = goalRepository.findByUserId(userId);

        List<GoalResponse> response = new ArrayList<>();
        for (Goal goal : goals) {
            List<GoalHistory> histories = fetchGoalHistoryByGoal(goal, zoneId);

            List<GoalHistoryResponse> historiesResponse = new ArrayList<>();
            for (GoalHistory history : histories) {
                GoalHistoryResponse historyResponse = GoalHistoryResponse.builder()
                        .date(history.getDate())
                        .amountGain(history.getAmountGain())
                        .amountGoal(history.getAmountGoal())
                        .build();
                historiesResponse.add(historyResponse);
            }

            historiesResponse.get(historiesResponse.size() - 1).setAmountGoal(goal.getAmount());

            GoalResponse goalResponse = goalMapper.mapToResponse(
                    goal,
                    historiesResponse.get(historiesResponse.size() - 1).getAmountGain(),
                    historiesResponse
            );

            response.add(goalResponse);
        }

        return SimpleListResponse.<GoalResponse>builder()
                .data(response)
                .build();
    }

    private List<GoalHistory> fetchGoalHistoryByGoal(Goal goal, ZoneId zoneId) {
        Calendar start = GoalTimeFrameHelper.getAuditStartCalendar(goal.getTimeFrame(), zoneId);

        List<GoalHistory> existingHistories =
                goalHistoryRepository.findAllByGoalIdAndDateGreaterThanEqual(
                        goal.getId(),
                        start.getTime()
                );

        Map<String, GoalHistory> result = new LinkedHashMap<>(); //to keep order of history

        List<Date> expectedDates = GoalTimeFrameHelper.generateExpectedDates(
                goal.getTimeFrame(),
                zoneId
        );
        for (Date date : expectedDates) {
            String key = GoalTimeFrameHelper.formatDateKey(date, goal.getTimeFrame());

            Long amountGoal = goal.getAmount();
            Instant goalCreationInstant = goal.getCreatedAt().atZone(zoneId).toInstant();
            Instant dateInstant = date.toInstant();

            if (goalCreationInstant.isAfter(dateInstant)) {
                amountGoal = 0L; // Goal wasn't active yet at this time, just record with zero target
            }

            result.put(
                    key,
                    GoalHistory.builder()
                            .dateKey(key)
                            .goal(goal)
                            .amountGain(0L)
                            .amountGoal(amountGoal)
                            .date(date)
                            .build()
            );
        }

        for (GoalHistory history : existingHistories) {
            result.put(history.getDateKey(), history);
        }

        return result.values().stream().toList();
    }

    @Override
    @Transactional
    public CreateGoalResponse createGoal(CreateGoalRequest request) {
        Sport sport = Common.findSportById(request.getSportId(), sportRepository);
        String userId = SecurityUtils.getCurrentUserId();

        Optional<Goal> goalOptional = goalRepository.findByUserIdAndSportIdAndTypeAndTimeFrame(
                userId,
                sport.getId(),
                request.getType(),
                request.getTimeFrame()
        );
        if (goalOptional.isPresent()) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.GOAL_IS_ALREADY_EXISTED);
        }

        Goal goal = goalMapper.mapToModel(request, sport, userId);
        goal = goalRepository.save(goal);

        return CreateGoalResponse.builder()
                .id(goal.getId())
                .build();
    }

    @Override
    @Transactional
    public void updateGoal(String id, ZoneId zoneId, UpdateGoalRequest request) {
        Goal goal = Common.findGoalById(id, goalRepository);

        if (request.getAmount() != null &&
                !Objects.equals(goal.getAmount(), request.getAmount())
        ) {
            updateGoalHistoryAmount(goal, zoneId, request.getAmount());
            goal.setAmount(request.getAmount());
        }

        // Need to call updateGoalHistoryStatus after updateGoalHistoryAmount
        // to avoid duplicate create GoalHistory
        if (request.getActive() != null &&
                !Objects.equals(goal.isActive(), request.getActive())
        ) {
            updateGoalHistoryStatus(goal, zoneId, request.getActive());
            goal.setActive(request.getActive());
        }

        goalRepository.save(goal);
    }

    private void updateGoalHistoryAmount(Goal goal, ZoneId zoneId, long amount) {
        List<GoalHistory> histories = fetchGoalHistoryByGoal(goal, zoneId);
        histories.get(histories.size() - 1).setAmountGoal(amount);
        goalHistoryRepository.saveAll(histories);
    }

    private void updateGoalHistoryStatus(Goal goal, ZoneId zoneId, boolean active) {
        Calendar calendar = GoalTimeFrameHelper.getCalendar(goal.getTimeFrame(), zoneId);
        String key = GoalTimeFrameHelper.formatDateKey(calendar.getTime(), goal.getTimeFrame());

        GoalHistory goalHistory = goalHistoryRepository.findByGoalIdAndDateKey(goal.getId(), key)
                .orElseGet(() -> createNewGoalHistory(goal, calendar));

        goalHistory.setAmountGoal(active ? goal.getAmount() : 0L);

        goalHistoryRepository.save(goalHistory);
    }

    private GoalHistory createNewGoalHistory(Goal goal, Calendar calendar) {
        Date date = calendar.getTime();
        String key = GoalTimeFrameHelper.formatDateKey(date, goal.getTimeFrame());

        return GoalHistory.builder()
                .dateKey(key)
                .goal(goal)
                .amountGain(0L)
                .amountGoal(goal.getAmount())
                .date(date)
                .build();
    }

    @Override
    @Transactional
    public void deleteGoal(String id) {
        Goal goal = Common.findGoalById(id, goalRepository);

        if (!goal.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new StrideException(HttpStatus.BAD_REQUEST, Message.CAN_NOTE_DELETE_OTHER_USER_GOAL);
        }

        // Remove links from activities to goal histories first
        goalHistoryRepository.deleteLinksToGoalHistories(id);

        // Then delete goal histories associated with the goal
        goalHistoryRepository.deleteByGoalId(id);

        // Finally, delete the goal itself
        goalRepository.delete(goal);
    }
}
