package com.example.BE_E_commerce.controller;


import com.example.BE_E_commerce.dto.request.CategoryRequest;
import com.example.BE_E_commerce.dto.response.CategoryResponse;
import com.example.BE_E_commerce.dto.response.MessageResponse;
import com.example. BE_E_commerce.service. CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Category Management", description = "Admin category management API")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Create new category
     */
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Update category
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update existing category")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Delete category
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id) {
        categoryService. deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }

    /**
     * Toggle category status
     */
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle category status", description = "Activate or deactivate a category")
    public ResponseEntity<CategoryResponse> toggleCategoryStatus(@PathVariable Long id) {
        CategoryResponse category = categoryService.toggleCategoryStatus(id);
        return ResponseEntity. ok(category);
    }
}