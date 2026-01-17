package com.example.BE_E_commerce.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

@Repository
public interface Shop extends JpaAttributeConverter<Shop,Long> {
}
