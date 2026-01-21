package com.example.BE_E_commerce.mapper;

import com.example.BE_E_commerce.dto.response. ProductImageResponse;
import com. example.BE_E_commerce. entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {

    ProductImageResponse toResponse(ProductImage image);

    List<ProductImageResponse> toResponseList(List<ProductImage> images);
}