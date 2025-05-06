package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.model.Sport;
import com.stride.tracking.dto.category.response.CategoryResponse;
import com.stride.tracking.dto.sport.request.CreateSportRequest;
import com.stride.tracking.dto.sport.request.RuleRequest;
import com.stride.tracking.dto.sport.response.RuleResponse;
import com.stride.tracking.dto.sport.response.SportResponse;
import org.springframework.stereotype.Component;

@Component
public class SportMapper {
    public Sport mapToModel(CreateSportRequest request, Category category) {
        return Sport.builder()
                .name(request.getName())
                .image(request.getImage())
                .category(category)
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

    public SportResponse mapToResponse(Sport sport, CategoryResponse category) {
        return SportResponse.builder()
                .id(sport.getId())
                .category(category)
                .name(sport.getName())
                .image(sport.getImage())
                .rules(sport.getRules().stream().map(this::mapToRuleResponse).toList())
                .sportMapType(sport.getSportMapType())
                .build();
    }
}
