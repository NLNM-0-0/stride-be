package com.stride.tracking.coreservice.mapper;

import com.stride.tracking.core.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.core.dto.category.response.CategoryResponse;
import com.stride.tracking.coreservice.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category mapToModel(CreateCategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
