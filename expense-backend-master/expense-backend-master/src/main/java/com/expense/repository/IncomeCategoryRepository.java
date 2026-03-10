package com.expense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expense.model.IncomeCategory;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, Long userId, Long id);
    Optional<IncomeCategory> findByNameIgnoreCaseAndUserId(String name, Long userId);
    Optional<IncomeCategory> findByNameIgnoreCaseAndUserIdIsNull(String name);
}
