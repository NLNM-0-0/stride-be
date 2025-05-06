package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.mapper.SportMapper;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.dto.sport.request.CreateSportRequest;
import com.stride.tracking.dto.sport.request.RuleRequest;
import com.stride.tracking.dto.sport.request.SportFilter;
import com.stride.tracking.dto.sport.request.UpdateSportRequest;
import com.stride.tracking.dto.sport.response.SportResponse;
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
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public ListResponse<SportResponse, SportFilter> getSports(AppPageRequest page, SportFilter filter) {
        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        Specification<Sport> spec = filterSports(filter);

        Page<Sport> sportPage = sportRepository.findAll(spec, pageable);

        List<Sport> sports = sportPage.getContent();

        List<SportResponse> data = sports.stream().map(sport ->
                sportMapper.mapToResponse(sport, categoryMapper.mapToCategoryResponse(sport.getCategory()))
        ).toList();

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
    @Transactional
    public SportResponse createSport(CreateSportRequest request) {
        validateRules(request.getRules());

        Category category = Common.findCategoryById(request.getCategoryId(), categoryRepository);

        Sport sport = sportMapper.mapToModel(request, category);

        sport = sportRepository.save(sport);

        return sportMapper.mapToResponse(
                sport,
                categoryMapper.mapToCategoryResponse(category)
        );
    }

    private void validateRules(List<RuleRequest> requests) {
        for (RuleRequest request : requests) {
            if (!CaloriesExpressionValidator.isValid(request.getExpression())){
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
        UpdateHelper.updateIfNotNull(request.getSportMapType(), sport::setSportMapType);

        sportRepository.save(sport);
    }

    @Override
    @Transactional
    public void deleteSport(String sportId) {
        Sport sport = Common.findSportById(sportId, sportRepository);

        sportRepository.delete(sport);
    }
}
