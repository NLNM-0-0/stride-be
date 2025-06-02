package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.configuration.kafka.KafkaProducer;
import com.stride.tracking.commons.constants.KafkaTopics;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.commons.dto.page.AppPageResponse;
import com.stride.tracking.commons.utils.UpdateHelper;
import com.stride.tracking.core.dto.category.event.CategoryUpdatedEvent;
import com.stride.tracking.core.dto.category.request.CategoryFilter;
import com.stride.tracking.core.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.core.dto.category.request.UpdateCategoryRequest;
import com.stride.tracking.core.dto.category.response.CategoryResponse;
import com.stride.tracking.coreservice.mapper.CategoryMapper;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.repository.CategoryRepository;
import com.stride.tracking.coreservice.repository.specs.CategorySpecs;
import com.stride.tracking.coreservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
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

    private final CategoryMapper categoryMapper;

    private final KafkaProducer kafkaProducer;

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

    @Override
    public SimpleListResponse<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        List<CategoryResponse> data = categories.stream()
                .map(categoryMapper::mapToCategoryResponse)
                .toList();

        return SimpleListResponse.<CategoryResponse>builder()
                .data(data)
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

        sendCategoryUpdatedEvent(KafkaTopics.CATEGORY_CREATED_TOPIC, category);

        return categoryMapper.mapToCategoryResponse(category);
    }

    private void sendCategoryUpdatedEvent(String topic, Category category) {
        kafkaProducer.send(
                topic,
                CategoryUpdatedEvent.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build()
        );
    }

    @Override
    @Transactional
    public void updateCategory(String categoryId, UpdateCategoryRequest request) {
        Category category = Common.findCategoryById(categoryId, categoryRepository);

        UpdateHelper.updateIfNotNull(request.getName(), category::setName);

        sendCategoryUpdatedEvent(KafkaTopics.CATEGORY_UPDATED_TOPIC, category);

        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = Common.findCategoryById(categoryId, categoryRepository);

        categoryRepository.delete(category);
    }
}
