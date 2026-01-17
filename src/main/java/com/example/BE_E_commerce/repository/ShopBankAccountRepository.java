package com.example.BE_E_commerce.repository;

import com.example.BE_E_commerce.entity.ShopBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopBankAccountRepository extends JpaRepository<ShopBankAccount,Long> {
}
