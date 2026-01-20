package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util. Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    // Find by slug
    Optional<Category> findBySlug(String slug);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Check if name exists
    boolean existsByName(String name);

    // Find all root categories (no parent)
    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    // Find children of a category
    List<Category> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    // Find all active categories
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Find active root categories
    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    // Find by level
    List<Category> findByLevel(Integer level);

    // Count products in category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    // Find all descendants of a category (including nested children)
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId OR c.parent.parent.id = :parentId")
    List<Category> findAllDescendants(@Param("parentId") Long parentId);

    // Check if category has children
    boolean existsByParentId(Long parentId);

}
