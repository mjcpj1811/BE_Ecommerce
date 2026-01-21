package com.example.BE_E_commerce.mapper;

import com.example.BE_E_commerce.dto.response.ProductDetailResponse;
import com.example. BE_E_commerce.dto. response.ProductResponse;
import com.example.BE_E_commerce.entity.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy. IGNORE,
        uses = {ProductVariantMapper.class, ProductImageMapper.class}
)
public interface ProductMapper {

    /**
     * Entity -> Response (Basic)
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "minPrice", ignore = true)
    @Mapping(target = "maxPrice", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "totalStock", ignore = true)
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    ProductResponse toResponse(Product product);

    /**
     * Entity -> Detail Response
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "minPrice", ignore = true)
    @Mapping(target = "maxPrice", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "totalStock", ignore = true)
    @Mapping(target = "averageRating", source = "averageRating")
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "relatedProducts", ignore = true)
    @Mapping(target = "recentReviews", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    ProductDetailResponse toDetailResponse(Product product);

    /**
     * List<Entity> -> List<Response>
     */
    @IterableMapping(qualifiedByName = "toBasicResponse")
    List<ProductResponse> toResponseList(List<Product> products);

    /**
     * Helper method with @Named to avoid ambiguity
     */
    @Named("toBasicResponse")
    default ProductResponse toBasicResponse(Product product) {
        return toResponse(product);
    }
}