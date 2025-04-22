package com.stride.tracking.coreservice.service;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.coreservice.payload.category.request.CategoryFilter;
import com.stride.tracking.coreservice.payload.category.request.CreateCategoryRequest;
import com.stride.tracking.coreservice.payload.category.request.UpdateCategoryRequest;
import com.stride.tracking.coreservice.payload.category.response.CategoryResponse;

public interface CategoryService {
    ListResponse<CategoryResponse, CategoryFilter> getCategories(
            AppPageRequest page,
            CategoryFilter filter
    );
    CategoryResponse createCategory(CreateCategoryRequest request);
    void updateCategory(String categoryId, UpdateCategoryRequest request);
    void deleteCategory(String categoryId);
}
