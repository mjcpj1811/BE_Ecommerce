package com.example.BE_E_commerce.mapper;

import com.example.BE_E_commerce.dto.response.ShopDetailResponse;
import com.example.BE_E_commerce.dto.response.ShopResponse;
import com.example.BE_E_commerce.entity.Shop;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShopMapper {

    /**
     * Entity -> Response
     */
    @Named("toResponse")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.fullName")
    ShopResponse toResponse(Shop shop);

    /**
     * Entity -> Detail Response
     */
    @Named("toDetailResponse")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.fullName")
    @Mapping(target = "featuredProducts", ignore = true)
    @Mapping(target = "bestSellers", ignore = true)
    @Mapping(target = "newArrivals", ignore = true)
    @Mapping(target = "categories", ignore = true)
    ShopDetailResponse toDetailResponse(Shop shop);

    /**
     * List<Entity> -> List<Response>
     */
    @IterableMapping(qualifiedByName = "toResponse")
    List<ShopResponse> toResponseList(List<Shop> shops);
}