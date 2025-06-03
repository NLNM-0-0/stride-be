package com.stride.tracking.metricservice.service.impl;

import com.stride.tracking.metric.dto.report.AuthProvider;
import com.stride.tracking.metric.dto.report.response.user.UserDetailReport;
import com.stride.tracking.metric.dto.report.response.user.UserReport;
import com.stride.tracking.metric.dto.user.event.UserCreatedEvent;
import com.stride.tracking.metricservice.mapper.UserMetricMapper;
import com.stride.tracking.metricservice.model.UserMetric;
import com.stride.tracking.metricservice.repository.UserMetricRepository;
import com.stride.tracking.metricservice.service.UserMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMetricServiceImpl implements UserMetricService {
    private final UserMetricRepository userMetricRepository;

    private final UserMetricMapper userMetricMapper;

    @Override
    public void saveMetrics(UserCreatedEvent event) {
        UserMetric userMetric = userMetricMapper.mapToModel(event);

        userMetricRepository.save(userMetric);
    }

    @Override
    public UserReport getUserReport(Instant from, Instant to) {
        List<UserMetric> users = userMetricRepository.findAllByTimeBetween(from, to);

        int[] numberUsersByProvider = new int[AuthProvider.values().length];
        for (UserMetric user : users) {
            numberUsersByProvider[user.getProvider().ordinal()]++;
        }

        List<UserDetailReport> userDetailReports = new ArrayList<>();
        for (int i = 0; i < AuthProvider.values().length; i++) {
            userDetailReports.add(
                    UserDetailReport.builder()
                            .provider(AuthProvider.values()[i])
                            .value(numberUsersByProvider[i])
                            .build()
            );
        }

        return UserReport.builder()
                .totalUsers(users.size())
                .users(userDetailReports)
                .build();
    }
}
