package com.example.BE_E_commerce.controller;

import com.example. BE_E_commerce.dto. response.CategoryResponse;
import com. example.BE_E_commerce. service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework. web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category public API")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all categories (flat list)
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all categories as a flat list")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService. getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Get all categories in hierarchical tree structure")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * Get active category tree (for public display)
     */
    @GetMapping("/active-tree")
    @Operation(summary = "Get active category tree", description = "Get only active categories in tree structure")
    public ResponseEntity<List<CategoryResponse>> getActiveCategoryTree() {
        List<CategoryResponse> tree = categoryService.getActiveCategoryTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get category details by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Get category by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Get category details by URL slug")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    /**
     * Get child categories
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Get all child categories of a parent category")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable Long id) {
        List<CategoryResponse> children = categoryService.getChildCategories(id);
        return ResponseEntity. ok(children);
    }
}