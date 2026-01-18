package com.example.BE_E_commerce.mapper;

import com.example.BE_E_commerce.dto.request.AddressRequest;
import com.example.BE_E_commerce.dto.response.AddressResponse;
import com.example.BE_E_commerce.entity.UserAddress;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    /**
     * Entity -> Response
     */
    AddressResponse toResponse(UserAddress address);

    /**
     * List<Entity> -> List<Response>
     */
    List<AddressResponse> toResponseList(List<UserAddress> addresses);

    /**
     * Request -> Entity (for create)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    UserAddress toEntity(AddressRequest request);

    /**
     * Update existing entity from request
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(AddressRequest request, @MappingTarget UserAddress address);
}