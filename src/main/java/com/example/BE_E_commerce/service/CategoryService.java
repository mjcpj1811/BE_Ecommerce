package com.example.BE_E_commerce.service;

import com.example.BE_E_commerce.dto.request.CategoryRequest;
import com.example.BE_E_commerce.dto.response.CategoryResponse;
import com.example.BE_E_commerce.entity.Category;
import com.example.BE_E_commerce.exception.BadRequestException;
import com.example.BE_E_commerce.exception.DuplicateResourceException;
import com.example.BE_E_commerce.exception.ResourceNotFoundException;
import com.example.BE_E_commerce.mapper.CategoryMapper;
import com.example.BE_E_commerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org. springframework.cache.annotation.Cacheable;
import org.springframework. stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util. ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ========== PUBLIC ENDPOINTS ==========

    /**
     * Get all categories (flat list)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category tree (hierarchical structure)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'tree'")
    public List<CategoryResponse> getCategoryTree() {
        // Get root categories
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();

        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    /**
     * Get active category tree (for public display)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'active-tree'")
    public List<CategoryResponse> getActiveCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

        return rootCategories.stream()
                .filter(Category::getIsActive)
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return toCategoryResponse(category);
    }

    /**
     * Get category by slug
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'slug: ' + #slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug:  " + slug));

        return toCategoryResponse(category);
    }

    /**
     * Get children of a category
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(Long parentId) {
        // Verify parent exists
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));

        List<Category> children = categoryRepository. findByParentIdOrderByDisplayOrderAsc(parentId);

        return children.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Create new category
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        // Check if name already exists
        if (categoryRepository. existsByName(request.getName())) {
            throw new DuplicateResourceException("Category name already exists:  " + request.getName());
        }

        Category category = categoryMapper.toEntity(request);

        // Generate slug
        category.setSlug(generateSlug(request.getName()));

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));

            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(0); // Root category
        }

        // Set display order
        if (request.getDisplayOrder() == null) {
            category.setDisplayOrder(getNextDisplayOrder(request.getParentId()));
        }

        category = categoryRepository.save(category);

        log.info("Category created:  {} (ID: {})", category.getName(), category.getId());

        return toCategoryResponse(category);
    }

    /**
     * Update category
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if name is being changed and if new name already exists
        if (! category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }

        // Update fields
        categoryMapper.updateEntityFromDto(request, category);

        // Update slug if name changed
        if (!category.getName().equals(request.getName())) {
            category.setSlug(generateSlug(request.getName()));
        }

        // Update parent if changed
        if (request.getParentId() != null) {
            if (! request.getParentId().equals(category.getParent() != null ? category.getParent().getId() : null)) {
                // Prevent setting self or descendant as parent
                if (request.getParentId().equals(id)) {
                    throw new BadRequestException("Category cannot be its own parent");
                }

                Category newParent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));

                // Check if new parent is a descendant
                if (isDescendant(newParent, category)) {
                    throw new BadRequestException("Cannot set a descendant as parent");
                }

                category.setParent(newParent);
                category.setLevel(newParent.getLevel() + 1);
            }
        } else {
            category.setParent(null);
            category.setLevel(0);
        }

        category = categoryRepository.save(category);

        log.info("Category updated: {} (ID: {})", category.getName(), category.getId());

        return toCategoryResponse(category);
    }

    /**
     * Delete category
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if category has products
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BadRequestException("Cannot delete category with " + productCount + " products.  Please move or delete products first.");
        }

        // Check if category has children
        if (categoryRepository.existsByParentId(id)) {
            throw new BadRequestException("Cannot delete category with subcategories. Please delete subcategories first.");
        }

        categoryRepository.delete(category);

        log.info("Category deleted: {} (ID: {})", category.getName(), category.getId());
    }

    /**
     * Toggle category active status
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setIsActive(!category.getIsActive());
        category = categoryRepository.save(category);

        log.info("Category status toggled: {} - Active: {}", category.getName(), category.getIsActive());

        return toCategoryResponse(category);
    }

    // ========== HELPER METHODS ==========

    /**
     * Build category tree recursively
     */
    private CategoryResponse buildCategoryTree(Category category) {
        CategoryResponse response = toCategoryResponse(category);

        // Get children
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(category. getId());

        if (! children.isEmpty()) {
            List<CategoryResponse> childResponses = children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            response.setChildren(childResponses);
        }

        return response;
    }

    /**
     * Convert entity to response with product count
     */
    private CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = categoryMapper.toResponse(category);

        // Get product count (including all subcategories)
        Long productCount = countProductsInCategoryTree(category.getId());
        response.setProductCount(productCount);

        return response;
    }

    /**
     * Count products in category and all its subcategories
     */
    private Long countProductsInCategoryTree(Long categoryId) {
        // Get all descendant category IDs
        List<Long> categoryIds = getAllDescendantIds(categoryId);
        
        // Count products in all these categories
        long totalCount = 0;
        for (Long id : categoryIds) {
            totalCount += categoryRepository.countProductsByCategoryId(id);
        }
        
        return totalCount;
    }

    /**
     * Generate URL-friendly slug from name
     */
    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Ensure uniqueness
        String originalSlug = slug;
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Get next display order for a parent
     */
    private Integer getNextDisplayOrder(Long parentId) {
        List<Category> siblings = parentId == null
                ? categoryRepository.findByParentIsNullOrderByDisplayOrderAsc()
                : categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId);

        if (siblings.isEmpty()) {
            return 0;
        }

        return siblings.stream()
                .mapToInt(Category::getDisplayOrder)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Check if a category is a descendant of another
     */
    private boolean isDescendant(Category potentialDescendant, Category ancestor) {
        Category current = potentialDescendant;
        while (current. getParent() != null) {
            if (current.getParent().getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Get all descendant IDs of a category (including the category itself)
     * For filtering products: parent category should show products from all subcategories
     */
    @Transactional(readOnly = true)
    public List<Long> getAllDescendantIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId); // Include the parent category itself
        
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            collectDescendantIds(category, ids);
        }
        
        return ids;
    }

    /**
     * Recursively collect all descendant IDs
     */
    private void collectDescendantIds(Category category, List<Long> ids) {
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(category.getId());
        for (Category child : children) {
            ids.add(child.getId());
            collectDescendantIds(child, ids); // Recursive call for nested subcategories
        }
    }
}