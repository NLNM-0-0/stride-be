package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.coreservice.constant.CacheName;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.dto.category.request.CategoryFilter;
import com.stride.tracking.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.dto.category.request.UpdateCategoryRequest;
import com.stride.tracking.dto.category.response.CategoryResponse;
import com.stride.tracking.coreservice.repository.CategoryRepository;
import com.stride.tracking.coreservice.repository.specs.CategorySpecs;
import com.stride.tracking.coreservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryCacheService categoryCacheService;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public ListResponse<CategoryResponse, CategoryFilter> getCategories(AppPageRequest page, CategoryFilter filter) {
        Pageable pageable = PageRequest.of(
                page.getPage() - 1,
                page.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        Specification<Category> spec = filterCategories(filter);

        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        List<Category> categories = categoryPage.getContent();

        List<CategoryResponse> data = categories.stream().map(categoryMapper::mapToCategoryResponse).toList();

        return ListResponse.<CategoryResponse, CategoryFilter>builder()
                .data(data)
                .appPageResponse(AppPageResponse.builder()
                        .index(page.getPage())
                        .limit(page.getLimit())
                        .totalPages(categoryPage.getTotalPages())
                        .totalElements(categoryPage.getTotalElements())
                        .build())
                .filter(filter)
                .build();
    }

    private Specification<Category> filterCategories(CategoryFilter filter) {
        Specification<Category> spec = Specification.where(null);
        if (filter.getName() != null) {
            spec = CategorySpecs.hasName(filter.getName());
        }
        return spec;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.mapToModel(request);

        category = categoryRepository.save(category);

        return categoryMapper.mapToCategoryResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheName.CATEGORY_BY_ID, key = "#categoryId")
    public void updateCategory(String categoryId, UpdateCategoryRequest request) {
        Category category = categoryCacheService.findById(categoryId);

        categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheName.CATEGORY_BY_ID, key = "#categoryId")
    public void deleteCategory(String categoryId) {
        Category category = categoryCacheService.findById(categoryId);

        categoryRepository.delete(category);
    }
}
