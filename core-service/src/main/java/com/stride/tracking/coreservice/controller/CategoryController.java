package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.coreservice.service.CategoryService;
import com.stride.tracking.dto.category.request.CategoryFilter;
import com.stride.tracking.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.dto.category.request.UpdateCategoryRequest;
import com.stride.tracking.dto.category.response.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    ResponseEntity<ListResponse<CategoryResponse, CategoryFilter>> getCategories(
            @Valid AppPageRequest page,
            @Valid CategoryFilter filter) {
        return ResponseEntity.ok(categoryService.getCategories(page, filter));
    }

    @PostMapping
    ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<SimpleResponse> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<SimpleResponse> deleteCategory(
            @PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
