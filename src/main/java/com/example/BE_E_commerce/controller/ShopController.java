package com.example.BE_E_commerce.controller;

import com.example.BE_E_commerce.dto.request.ShopFilterRequest;
import com.example.BE_E_commerce.dto.response.PageResponse;
import com.example.BE_E_commerce.dto.response.ProductResponse;
import com.example.BE_E_commerce.dto.response.ShopDetailResponse;
import com.example.BE_E_commerce.dto.response.ShopResponse;
import com.example.BE_E_commerce.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Shop public API")
public class ShopController {

    private final ShopService shopService;

    /**
     * Get all shops with filters
     */
    @GetMapping
    @Operation(summary = "Get all shops", description = "Get shops with filters, search, sorting and pagination")
    public ResponseEntity<PageResponse<ShopResponse>> getAllShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ShopFilterRequest filter = new ShopFilterRequest();
        filter.setKeyword(keyword);
        filter.setStatus(status);
        filter.setMinRating(minRating);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        filter.setPage(page);
        filter.setSize(size);

        PageResponse<ShopResponse> shops = shopService.getAllShops(filter);
        return ResponseEntity.ok(shops);
    }

    /**
     * Search shops
     */
    @GetMapping("/search")
    @Operation(summary = "Search shops", description = "Search shops by keyword")
    public ResponseEntity<PageResponse<ShopResponse>> searchShops(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<ShopResponse> shops = shopService.searchShops(keyword, pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * Get shop by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get shop by ID", description = "Get shop details by ID")
    public ResponseEntity<ShopDetailResponse> getShopById(@PathVariable Long id) {
        ShopDetailResponse shop = shopService.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    /**
     * Get shop by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get shop by slug", description = "Get shop details by URL slug")
    public ResponseEntity<ShopDetailResponse> getShopBySlug(@PathVariable String slug) {
        ShopDetailResponse shop = shopService.getShopBySlug(slug);
        return ResponseEntity.ok(shop);
    }

    /**
     * Get shop products
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "Get shop products", description = "Get all products from a shop")
    public ResponseEntity<PageResponse<ProductResponse>> getShopProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<ProductResponse> products = shopService.getShopProducts(id, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get top rated shops
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated shops", description = "Get shops with highest ratings")
    public ResponseEntity<PageResponse<ShopResponse>> getTopRatedShops(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ShopResponse> shops = shopService.getTopRatedShops(pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * Get best seller shops
     */
    @GetMapping("/best-sellers")
    @Operation(summary = "Get best seller shops", description = "Get shops with most sales")
    public ResponseEntity<PageResponse<ShopResponse>> getBestSellerShops(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ShopResponse> shops = shopService.getBestSellerShops(pageable);
        return ResponseEntity.ok(shops);
    }
}