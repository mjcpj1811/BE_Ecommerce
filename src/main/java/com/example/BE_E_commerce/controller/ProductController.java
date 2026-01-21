package com.example.BE_E_commerce.controller;

import com.example. BE_E_commerce.dto. request.ProductFilterRequest;
import com.example.BE_E_commerce.dto.response.*;
import com.example.BE_E_commerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework. data.domain.Pageable;
import org.springframework.data. domain.Sort;
import org. springframework.http.ResponseEntity;
import org.springframework. web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product public API")
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with filters
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Get products with filters, search, sorting and pagination")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setKeyword(keyword);
        filter.setCategoryId(categoryId);
        filter.setShopId(shopId);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setMinRating(minRating);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        filter.setPage(page);
        filter.setSize(size);

        PageResponse<ProductResponse> products = productService.getAllProducts(filter);
        return ResponseEntity.ok(products);
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ProductResponse> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get product details by ID")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        ProductDetailResponse product = productService.getProductById(id);
        return ResponseEntity. ok(product);
    }

    /**
     * Get product by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Get product details by URL slug")
    public ResponseEntity<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse product = productService.getProductBySlug(slug);
        return ResponseEntity. ok(product);
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Get all products in a category")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by shop
     */
    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Get products by shop", description = "Get all products from a shop")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductResponse> products = productService.getProductsByShop(shopId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get best sellers
     */
    @GetMapping("/best-sellers")
    @Operation(summary = "Get best sellers", description = "Get top selling products")
    public ResponseEntity<PageResponse<ProductResponse>> getBestSellers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductResponse> products = productService.getBestSellers(pageable);
        return ResponseEntity. ok(products);
    }

    /**
     * Get new arrivals
     */
    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrivals", description = "Get newest products")
    public ResponseEntity<PageResponse<ProductResponse>> getNewArrivals(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest. of(page, size);
        PageResponse<ProductResponse> products = productService.getNewArrivals(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get top rated products
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated", description = "Get highest rated products")
    public ResponseEntity<PageResponse<ProductResponse>> getTopRated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest. of(page, size);
        PageResponse<ProductResponse> products = productService.getTopRated(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product variants
     */
    @GetMapping("/{id}/variants")
    @Operation(summary = "Get product variants", description = "Get all variants of a product")
    public ResponseEntity<List<ProductVariantResponse>> getProductVariants(@PathVariable Long id) {
        List<ProductVariantResponse> variants = productService.getProductVariants(id);
        return ResponseEntity.ok(variants);
    }

    /**
     * Get product images
     */
    @GetMapping("/{id}/images")
    @Operation(summary = "Get product images", description = "Get all images of a product")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long id) {
        List<ProductImageResponse> images = productService.getProductImages(id);
        return ResponseEntity.ok(images);
    }

    /**
     * Get product reviews
     */
    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get product reviews", description = "Get all reviews of a product")
    public ResponseEntity<PageResponse<ReviewResponse>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ReviewResponse> reviews = productService.getProductReviews(id, pageable);
        return ResponseEntity.ok(reviews);
    }
}