package com.example.BE_E_commerce.mapper;


import com.example.BE_E_commerce.dto.response.ProductVariantResponse;
import com.example. BE_E_commerce.entity. ProductVariant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    ProductVariantResponse toResponse(ProductVariant variant);

    List<ProductVariantResponse> toResponseList(List<ProductVariant> variants);
}