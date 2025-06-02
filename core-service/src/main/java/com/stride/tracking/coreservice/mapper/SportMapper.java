package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.core.dto.sport.request.CreateSportRequest;
import com.stride.tracking.core.dto.sport.request.RuleRequest;
import com.stride.tracking.core.dto.sport.response.RuleResponse;
import com.stride.tracking.core.dto.sport.response.SportResponse;
import com.stride.tracking.core.dto.sport.response.SportShortResponse;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.model.Sport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SportMapper {
    private final CategoryMapper categoryMapper;

    public Sport mapToModel(CreateSportRequest request, Category category) {
        return Sport.builder()
                .name(request.getName())
                .image(request.getImage())
                .category(category)
                .color(request.getColor())
                .rules(request.getRules().stream().map(this::mapToModel).toList())
                .sportMapType(request.getSportMapType())
                .build();
    }

    public Rule mapToModel(RuleRequest request) {
        return Rule.builder()
                .expression(request.getExpression())
                .met(request.getMet())
                .build();
    }

    public RuleResponse mapToRuleResponse(Rule rule) {
        return RuleResponse.builder()
                .expression(rule.getExpression())
                .met(rule.getMet())
                .build();
    }

    public SportResponse mapToResponse(Sport sport) {
        return SportResponse.builder()
                .id(sport.getId())
                .category(categoryMapper.mapToCategoryResponse(sport.getCategory()))
                .name(sport.getName())
                .image(sport.getImage())
                .rules(sport.getRules().stream().map(this::mapToRuleResponse).toList())
                .sportMapType(sport.getSportMapType())
                .color(sport.getColor())
                .build();
    }

    public SportShortResponse mapToShortResponse(Sport sport) {
        return SportShortResponse.builder()
                .id(sport.getId())
                .name(sport.getName())
                .category(categoryMapper.mapToCategoryResponse(sport.getCategory()))
                .image(sport.getImage())
                .color(sport.getColor())
                .sportMapType(sport.getSportMapType())
                .build();
    }
}
