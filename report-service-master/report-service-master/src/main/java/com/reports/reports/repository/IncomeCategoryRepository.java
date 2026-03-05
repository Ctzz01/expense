package com.reports.reports.repository;

import com.reports.reports.model.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {
    Optional<IncomeCategory> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);
    Optional<IncomeCategory> findByNameIgnoreCaseAndUserId(String name, Long userId);
    Optional<IncomeCategory> findByNameIgnoreCaseAndUserIdIsNull(String name);
}
