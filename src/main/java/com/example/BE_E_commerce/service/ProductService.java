package com.example.BE_E_commerce.service;

import com.example. BE_E_commerce.dto. request.ProductFilterRequest;
import com.example. BE_E_commerce.dto. response.*;
import com.example.BE_E_commerce.entity.Product;
import com.example.BE_E_commerce.entity.ProductImage;
import com.example.BE_E_commerce.enums. ProductStatus;
import com.example.BE_E_commerce.enums.ReviewStatus;
import com.example. BE_E_commerce.exception.ResourceNotFoundException;
import com. example.BE_E_commerce. mapper.ProductImageMapper;
import com.example. BE_E_commerce.mapper. ProductMapper;
import com.example.BE_E_commerce.mapper.ProductVariantMapper;
import com.example.BE_E_commerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data. domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation. Transactional;

import java.math.BigDecimal;
import java.util. List;
import java.util. stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final ProductImageMapper imageMapper;
    private final CategoryService categoryService;

    // ========== PUBLIC ENDPOINTS ==========

    /**
     * Get all products with filters and pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        Page<Product> productPage;

        // Get all category IDs if categoryId filter is provided (parent + subcategories)
        List<Long> categoryIds = null;
        if (filter.getCategoryId() != null) {
            categoryIds = categoryService.getAllDescendantIds(filter.getCategoryId());
        }

        if (hasFilters(filter)) {
            // Search with filters
            if ("price".equalsIgnoreCase(filter.getSortBy())) {
                // Use special price sorting methods
                Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
                if ("ASC".equalsIgnoreCase(filter.getSortDirection())) {
                    productPage = productRepository.searchWithFiltersOrderByPriceAsc(
                            filter.getKeyword(),
                            categoryIds,
                            filter.getShopId(),
                            filter.getMinPrice(),
                            filter.getMaxPrice(),
                            ProductStatus.ACTIVE,
                            pageable
                    );
                } else {
                    productPage = productRepository.searchWithFiltersOrderByPriceDesc(
                            filter.getKeyword(),
                            categoryIds,
                            filter.getShopId(),
                            filter.getMinPrice(),
                            filter.getMaxPrice(),
                            ProductStatus.ACTIVE,
                            pageable
                    );
                }
            } else {
                // Use regular sorting for other fields
                Pageable pageable = createPageable(filter);
                productPage = productRepository.searchWithFilters(
                        filter.getKeyword(),
                        categoryIds,
                        filter.getShopId(),
                        filter.getMinPrice(),
                        filter.getMaxPrice(),
                        ProductStatus.ACTIVE,
                        pageable
                );
            }
        } else {
            // Get all active products
            Pageable pageable = createPageable(filter);
            productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> productPage = productRepository.searchByName(
                keyword,
                ProductStatus.ACTIVE,
                pageable
        );

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Only return active products to public
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found or inactive");
        }

        return enrichProductDetailResponse(product);
    }

    /**
     * Get product by slug
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'slug:' + #slug")
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug:  " + slug));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found or inactive");
        }

        return enrichProductDetailResponse(product);
    }

    /**
     * Get products by category (includes subcategories)
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        // Get all category IDs (parent + all descendants)
        List<Long> categoryIds = categoryService.getAllDescendantIds(categoryId);
        
        Page<Product> productPage = productRepository.findByCategoryIdInAndStatus(
                categoryIds,
                ProductStatus.ACTIVE,
                pageable
        );

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get products by shop
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByShop(Long shopId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByShopId(shopId, pageable);

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get best seller products
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'bestsellers:' + #pageable.pageNumber")
    public PageResponse<ProductResponse> getBestSellers(Pageable pageable) {
        Page<Product> productPage = productRepository.findBestSellers(
                ProductStatus.ACTIVE,
                pageable
        );

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get new arrival products
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'newarrivals:' + #pageable.pageNumber")
    public PageResponse<ProductResponse> getNewArrivals(Pageable pageable) {
        Page<Product> productPage = productRepository.findNewArrivals(
                ProductStatus.ACTIVE,
                pageable
        );

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get top rated products
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'toprated:' + #pageable.pageNumber")
    public PageResponse<ProductResponse> getTopRated(Pageable pageable) {
        Page<Product> productPage = productRepository.findTopRated(
                ProductStatus.ACTIVE,
                4.0, // Min rating
                pageable
        );

        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::enrichProductResponse)
                .collect(Collectors.toList());

        return buildPageResponse(products, productPage);
    }

    /**
     * Get product variants
     */
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getProductVariants(Long productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId).stream()
                .map(variantMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product images
     */
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        return imageRepository.findByProductIdOrderByDisplayOrderAsc(productId).stream()
                .map(imageMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product reviews
     */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<com.example.BE_E_commerce.entity.Review> reviewPage =
                reviewRepository.findByProductIdAndStatus(
                        productId,
                        ReviewStatus.APPROVED,
                        pageable
                );

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());

        return buildPageResponse(reviews, reviewPage);
    }

    // ========== HELPER METHODS ==========

    /**
     * Enrich product response with additional data
     */
    private ProductResponse enrichProductResponse(Product product) {
        ProductResponse response = productMapper.toResponse(product);

        // Get pricing from variants
        BigDecimal minPrice = variantRepository.findMinPriceByProductId(product.getId());
        BigDecimal maxPrice = variantRepository.findMaxPriceByProductId(product.getId());
        response.setMinPrice(minPrice);
        response.setMaxPrice(maxPrice);

        // Get total stock
        Integer totalStock = variantRepository.sumStockByProductId(product.getId());
        response.setTotalStock(totalStock != null ? totalStock : 0);

        // Get rating and review count
        Double averageRating = reviewRepository.calculateAverageRating(
                product.getId(),
                ReviewStatus.APPROVED
        );
        response.setAverageRating(averageRating != null ? averageRating : 0.0);

        Long reviewCount = reviewRepository.countByProductIdAndStatus(
                product.getId(),
                ReviewStatus.APPROVED
        );
        response.setReviewCount(reviewCount);

        // Get thumbnail
        ProductImage thumbnail = imageRepository.findFirstByProductId(product.getId())
                .orElse(null);
        if (thumbnail != null) {
            response.setThumbnailUrl(thumbnail.getImageUrl());
        }

        // Get variants and images
        response.setVariants(variantMapper.toResponseList(
                variantRepository.findByProductIdAndIsActiveTrue(product. getId())
        ));
        response.setImages(imageMapper. toResponseList(
                imageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId())
        ));

        return response;
    }

    /**
     * Enrich product detail response
     */
    private ProductDetailResponse enrichProductDetailResponse(Product product) {
        ProductDetailResponse response = productMapper.toDetailResponse(product);

        // Add all enrichments from basic response
        ProductResponse basicResponse = enrichProductResponse(product);
        response.setMinPrice(basicResponse.getMinPrice());
        response.setMaxPrice(basicResponse.getMaxPrice());
        response.setTotalStock(basicResponse.getTotalStock());
        response.setAverageRating(basicResponse.getAverageRating());
        response.setReviewCount(basicResponse.getReviewCount());
        response.setThumbnailUrl(basicResponse.getThumbnailUrl());
        response.setVariants(basicResponse. getVariants());
        response.setImages(basicResponse.getImages());

        // Get related products (same category)
        List<Product> relatedProducts = productRepository.findRelatedProducts(
                product.getCategory().getId(),
                product. getId(),
                ProductStatus. ACTIVE,
                PageRequest.of(0, 10)
        );
        response.setRelatedProducts(
                relatedProducts.stream()
                        .map(this::enrichProductResponse)
                        .collect(Collectors.toList())
        );

        // Get recent reviews
        List<com.example.BE_E_commerce.entity.Review> recentReviews =
                reviewRepository.findTop5ByProductIdAndStatusOrderByCreatedAtDesc(
                        product.getId(),
                        ReviewStatus. APPROVED
                );
        response.setRecentReviews(
                recentReviews.stream()
                        .map(this:: toReviewResponse)
                        .collect(Collectors.toList())
        );

        return response;
    }

    /**
     * Convert Review entity to ReviewResponse
     */
    private ReviewResponse toReviewResponse(com. example.BE_E_commerce. entity.Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review. getComment());
        response.setUserName(review.getUser().getFullName());
        response.setUserAvatar(review.getUser().getAvatarUrl());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    /**
     * Create pageable with sorting
     */
    private Pageable createPageable(ProductFilterRequest filter) {
        Sort sort = Sort.by(
                filter.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                filter.getSortBy()
        );

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    /**
     * Check if filter has any criteria
     */
    private boolean hasFilters(ProductFilterRequest filter) {
        return filter.getKeyword() != null ||
                filter.getCategoryId() != null ||
                filter.getShopId() != null ||
                filter.getMinPrice() != null ||
                filter.getMaxPrice() != null;
    }

    /**
     * Build page response
     */
    private <T> PageResponse<T> buildPageResponse(List<T> content, Page<? > page) {
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }
}