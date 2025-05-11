package com.stride.tracking.coreservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.CacheName;
import com.stride.tracking.coreservice.constant.Message;
import com.stride.tracking.coreservice.model.Category;
import com.stride.tracking.coreservice.repository.CategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryCacheService {
    private final CategoryRepository categoryRepository;

    @Cacheable(value = CacheName.CATEGORY_BY_ID, key = "#categoryId")
    @Transactional(readOnly = true)
    public Category findById(String categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.CATEGORY_NOT_FOUND)
        );
    }
}
