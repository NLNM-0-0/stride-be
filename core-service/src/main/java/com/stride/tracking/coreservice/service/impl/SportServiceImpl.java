package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.core.dto.sport.event.SportUpdatedEvent;
import com.stride.tracking.core.dto.sport.request.CreateSportRequest;
import com.stride.tracking.core.dto.sport.request.RuleRequest;
import com.stride.tracking.core.dto.sport.request.SportFilter;
import com.stride.tracking.core.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.core.dto.sport.response.SportResponse;
import com.stride.tracking.core.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.coreservice.repository.CategoryRepository;
import com.stride.tracking.coreservice.repository.SportRepository;
import com.stride.tracking.coreservice.repository.specs.SportSpecs;
import com.stride.tracking.coreservice.service.SportService;
import com.stride.tracking.coreservice.utils.validator.CaloriesExpressionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {
    private final SportRepository sportRepository;

    private final CategoryRepository categoryRepository;

    private final SportMapper sportMapper;

    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<SportResponse, SportFilter> getSports(AppPageRequest page, SportFilter filter) {
        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        Specification<Sport> spec = filterSports(filter);

        Page<Sport> sportPage = sportRepository.findAll(spec, pageable);

        List<Sport> sports = sportPage.getContent();

        List<SportResponse> data = sports.stream()
                .map(sportMapper::mapToResponse)
                .toList();

        return ListResponse.<SportResponse, SportFilter>builder()
                .data(data)
                .appPageResponse(AppPageResponse.builder()
                        .index(page.getPage())
                        .limit(page.getLimit())
                        .totalPages(sportPage.getTotalPages())
                        .totalElements(sportPage.getTotalElements())
                        .build())
                .filter(filter)
                .build();
    }

    private Specification<Sport> filterSports(SportFilter filter) {
        Specification<Sport> spec = Specification.where(null);
        if (filter.getName() != null) {
            spec = SportSpecs.hasName(filter.getName());
        }
        if (filter.getCategoryId() != null) {
            spec = spec.and(SportSpecs.hasCategory(filter.getCategoryId()));
        }
        return spec;
    }

    @Override
    @Transactional(readOnly = true)
    public SimpleListResponse<SportShortResponse> getSports() {
        List<Sport> sports = sportRepository.findAll();

        List<SportShortResponse> data = sports.stream()
                .map(sportMapper::mapToShortResponse)
                .toList();

        return SimpleListResponse.<SportShortResponse>builder()
                .data(data)
                .build();
    }

    @Override
    @Transactional
    public SportResponse createSport(CreateSportRequest request) {
        validateRules(request.getRules());

        Category category = Common.findCategoryById(request.getCategoryId(), categoryRepository);

        Sport sport = sportMapper.mapToModel(request, category);

        sport = sportRepository.save(sport);

        sendSportUpdatedMetric(KafkaTopics.SPORT_CREATED_TOPIC, sport);

        return sportMapper.mapToResponse(sport);
    }

    private void sendSportUpdatedMetric(String topic, Sport sport) {
        kafkaProducer.send(
                topic,
                SportUpdatedEvent.builder()
                        .id(sport.getId())
                        .name(sport.getName())
                        .image(sport.getImage())
                        .color(sport.getColor())
                        .categoryId(sport.getCategory().getId())
                        .sportMapType(sport.getSportMapType())
                        .build()
        );
    }

    private void validateRules(List<RuleRequest> requests) {
        for (RuleRequest request : requests) {
            if (!CaloriesExpressionValidator.isValid(request.getExpression())) {
                throw new StrideException(HttpStatus.BAD_REQUEST, Message.RULE_INVALID);
            }
        }
    }

    @Override
    @Transactional
    public void updateSport(String sportId, UpdateSportRequest request) {
        Sport sport = Common.findSportById(sportId, sportRepository);

        if (request.getRules() != null) {
            validateRules(request.getRules());

            List<Rule> rules = request.getRules().stream().map(sportMapper::mapToModel).toList();
            sport.setRules(rules);
        }

        if (request.getCategoryId() != null) {
            Category category = Common.findCategoryById(request.getCategoryId(), categoryRepository);
            sport.setCategory(category);
        }

        UpdateHelper.updateIfNotNull(request.getName(), sport::setName);
        UpdateHelper.updateIfNotNull(request.getImage(), sport::setImage);
        UpdateHelper.updateIfNotNull(request.getColor(), sport::setColor);
        UpdateHelper.updateIfNotNull(request.getSportMapType(), sport::setSportMapType);

        Sport updatedSport = sportRepository.save(sport);

        sendSportUpdatedMetric(KafkaTopics.SPORT_UPDATED_TOPIC, updatedSport);
    }

    @Override
    @Transactional
    public void deleteSport(String sportId) {
        Sport sport = Common.findSportById(sportId, sportRepository);

        sportRepository.delete(sport);
    }
}
