package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.ShopBalance;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopBalanceRepository  extends JpaAttributeConverter<ShopBalance,Long> {
}
