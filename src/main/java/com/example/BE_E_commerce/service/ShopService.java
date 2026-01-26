package com.example.BE_E_commerce.service;


import com.example.BE_E_commerce.dto.request.ShopFilterRequest;
import com.example.BE_E_commerce.dto.response.*;
import com.example.BE_E_commerce.entity.Product;
import com.example.BE_E_commerce.entity.Shop;
import com.example.BE_E_commerce.enums.ProductStatus;
import com.example.BE_E_commerce.enums.ShopStatus;
import com.example.BE_E_commerce.exception.ResourceNotFoundException;
import com.example.BE_E_commerce.mapper.ProductMapper;
import com.example.BE_E_commerce.mapper.ShopMapper;
import com.example.BE_E_commerce.repository.ProductRepository;
import com.example.BE_E_commerce.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ShopMapper shopMapper;
    private final ProductMapper productMapper;

    // ========== PUBLIC ENDPOINTS ==========

    /**
     * Get all shops with filters and pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ShopResponse> getAllShops(ShopFilterRequest filter) {
        Pageable pageable = createPageable(filter);

        Page<Shop> shopPage;

        if (hasFilters(filter)) {
            // Search with filters
            ShopStatus status = filter.getStatus() != null ?
                    ShopStatus.valueOf(filter.getStatus()) : ShopStatus.ACTIVE;

            shopPage = shopRepository.searchWithFilters(
                    filter.getKeyword(),
                    status,
                    filter.getMinRating(),
                    pageable
            );
        } else {
            // Get all active shops
            shopPage = shopRepository.findByStatus(ShopStatus.ACTIVE, pageable);
        }

        List<ShopResponse> shops = shopPage.getContent().stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());

        return buildPageResponse(shops, shopPage);
    }

    /**
     * Search shops by keyword
     */
    @Transactional(readOnly = true)
    public PageResponse<ShopResponse> searchShops(String keyword, Pageable pageable) {
        Page<Shop> shopPage = shopRepository.searchByName(
                keyword,
                ShopStatus.ACTIVE,
                pageable
        );

        List<ShopResponse> shops = shopPage.getContent().stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());

        return buildPageResponse(shops, shopPage);
    }

    /**
     * Get shop by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "shops", key = "#id")
    public ShopDetailResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", id));

        // Only return active shops to public
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException("Shop not found or inactive");
        }

        return enrichShopDetailResponse(shop);
    }

    /**
     * Get shop by slug
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "shops", key = "'slug:' + #slug")
    public ShopDetailResponse getShopBySlug(String slug) {
        Shop shop = shopRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with slug: " + slug));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException("Shop not found or inactive");
        }

        return enrichShopDetailResponse(shop);
    }

    /**
     * Get products of a shop
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getShopProducts(Long shopId, Pageable pageable) {
        // Verify shop exists and is active
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new ResourceNotFoundException("Shop not found or inactive");
        }

        List<Product> products = productRepository.findByShopIdAndStatus(
                shopId,
                ProductStatus.ACTIVE,
                pageable
        );

        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(productResponses)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements((long) products.size())
                .totalPages(1)
                .isLast(true)
                .isFirst(true)
                .build();
    }

    /**
     * Get top rated shops
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "shops", key = "'toprated:' + #pageable.pageNumber")
    public PageResponse<ShopResponse> getTopRatedShops(Pageable pageable) {
        Page<Shop> shopPage = shopRepository.findTopRated(
                ShopStatus.ACTIVE,
                4.0, // Min rating
                pageable
        );

        List<ShopResponse> shops = shopPage.getContent().stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());

        return buildPageResponse(shops, shopPage);
    }

    /**
     * Get best seller shops
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "shops", key = "'bestsellers:' + #pageable.pageNumber")
    public PageResponse<ShopResponse> getBestSellerShops(Pageable pageable) {
        Page<Shop> shopPage = shopRepository.findBestSellers(
                ShopStatus.ACTIVE,
                pageable
        );

        List<ShopResponse> shops = shopPage.getContent().stream()
                .map(shopMapper::toResponse)
                .collect(Collectors.toList());

        return buildPageResponse(shops, shopPage);
    }

    // ========== HELPER METHODS ==========

    /**
     * Enrich shop detail response with products and categories
     */
    private ShopDetailResponse enrichShopDetailResponse(Shop shop) {
        ShopDetailResponse response = shopMapper.toDetailResponse(shop);

        // Get featured products (top rated, limited to 10)
        Pageable featuredPageable = PageRequest.of(0, 10, Sort.by("averageRating").descending());
        List<Product> featuredProducts = productRepository.findByShopIdAndStatus(
                shop.getId(),
                ProductStatus.ACTIVE,
                featuredPageable
        );
        response.setFeaturedProducts(
                featuredProducts.stream()
                        .map(productMapper::toResponse)
                        .collect(Collectors.toList())
        );

        // Get best sellers (top sold, limited to 5)
        Pageable bestSellersPageable = PageRequest.of(0, 5, Sort.by("totalSold").descending());
        List<Product> bestSellers = productRepository.findByShopIdAndStatus(
                shop.getId(),
                ProductStatus.ACTIVE,
                bestSellersPageable
        );
        response.setBestSellers(
                bestSellers.stream()
                        .map(productMapper::toResponse)
                        .collect(Collectors.toList())
        );

        // Get new arrivals (latest products, limited to 5)
        Pageable newArrivalsPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<Product> newArrivals = productRepository.findByShopIdAndStatus(
                shop.getId(),
                ProductStatus.ACTIVE,
                newArrivalsPageable
        );
        response.setNewArrivals(
                newArrivals.stream()
                        .map(productMapper::toResponse)
                        .collect(Collectors.toList())
        );

        // Get categories with product count
        List<Product> allProducts = productRepository.findByShopIdAndStatus(
                shop.getId(),
                ProductStatus.ACTIVE,
                Pageable.unpaged()
        );

        Map<Long, Long> categoryProductCounts = allProducts.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getId(),
                        Collectors.counting()
                ));

        List<ShopDetailResponse.CategorySummary> categories = categoryProductCounts.entrySet().stream()
                .map(entry -> {
                    Product sampleProduct = allProducts.stream()
                            .filter(p -> p.getCategory().getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);

                    if (sampleProduct != null) {
                        ShopDetailResponse.CategorySummary summary = new ShopDetailResponse.CategorySummary();
                        summary.setId(sampleProduct.getCategory().getId());
                        summary.setName(sampleProduct.getCategory().getName());
                        summary.setSlug(sampleProduct.getCategory().getSlug());
                        summary.setProductCount(entry.getValue());
                        return summary;
                    }
                    return null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());

        response.setCategories(categories);

        return response;
    }

    /**
     * Create pageable with sorting
     */
    private Pageable createPageable(ShopFilterRequest filter) {
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
    private boolean hasFilters(ShopFilterRequest filter) {
        return filter.getKeyword() != null ||
                filter.getStatus() != null ||
                filter.getMinRating() != null;
    }

    /**
     * Build page response
     */
    private <T> PageResponse<T> buildPageResponse(List<T> content, Page<?> page) {
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