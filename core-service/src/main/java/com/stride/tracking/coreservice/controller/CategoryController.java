package com.stride.tracking.coreservice.controller;

import com.stride.tracking.commons.annotations.PreAuthorizeAdmin;
import com.stride.tracking.commons.dto.ListResponse;
import com.stride.tracking.commons.dto.SimpleListResponse;
import com.stride.tracking.commons.dto.SimpleResponse;
import com.stride.tracking.commons.dto.page.AppPageRequest;
import com.stride.tracking.core.dto.category.request.CategoryFilter;
import com.stride.tracking.core.dto.category.request.CreateCategoryRequest;
import com.stride.tracking.core.dto.category.request.UpdateCategoryRequest;
import com.stride.tracking.core.dto.category.response.CategoryResponse;
import com.stride.tracking.coreservice.service.CategoryService;
import jakarta.annotation.security.PermitAll;
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

    @GetMapping("/manage")
    @PreAuthorizeAdmin
    ResponseEntity<ListResponse<CategoryResponse, CategoryFilter>> getCategories(
            @Valid AppPageRequest page,
            @Valid CategoryFilter filter) {
        return ResponseEntity.ok(categoryService.getCategories(page, filter));
    }

    @GetMapping("/all")
    @PermitAll
    ResponseEntity<SimpleListResponse<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping("/manage")
    @PreAuthorizeAdmin
    ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/manage/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new SimpleResponse());
    }

    @DeleteMapping("/manage/{id}")
    @PreAuthorizeAdmin
    ResponseEntity<SimpleResponse> deleteCategory(
            @PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new SimpleResponse());
    }
}
