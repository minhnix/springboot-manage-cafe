package com.nix.managecafe.controller;

import com.nix.managecafe.model.Category;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.payload.response.ApiResponse;
import com.nix.managecafe.payload.response.MenuDetailResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.service.CategoryService;
import com.nix.managecafe.service.MenuService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuService menuService;

    @GetMapping
    public PagedResponse<Category> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return categoryService.getAll(page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public Category getOne(@PathVariable("id") Long id) {
        return categoryService.getOne(id);
    }

    @GetMapping("/{id}/menu")
    public PagedResponse<Menu> getMenuByCategoryId(@PathVariable("id") Long categoryId,
                                                                 @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                 @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
                                                                 @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
                                                                 @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return menuService.getMenuByCategoryId(categoryId, page, size, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Category category) {
        Category category1 = categoryService.create(category);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(category1.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Category created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Valid @RequestBody Category category) {
        categoryService.update(id, category);
        return ResponseEntity.ok(new ApiResponse(true, "Category updated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
    }
}
