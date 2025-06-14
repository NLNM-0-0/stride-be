package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.category.request.CategoryFilter;
import com.stride.tracking.core.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.core.dto.category.request.UpdateCategoryRequest;
import com.stride.tracking.core.dto.category.response.CategoryResponse;

public interface CategoryService {
    ListResponse<CategoryResponse, CategoryFilter> getCategories(
            AppPageRequest page,
            CategoryFilter filter
    );
    SimpleListResponse<CategoryResponse> getCategories();
    CategoryResponse createCategory(CreateCategoryRequest request);
    void updateCategory(String categoryId, UpdateCategoryRequest request);
    void deleteCategory(String categoryId);
}
