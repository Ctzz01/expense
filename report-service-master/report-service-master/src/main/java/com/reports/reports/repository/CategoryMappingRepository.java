package com.reports.reports.repository;

import com.reports.reports.model.CategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CategoryMappingRepository extends JpaRepository<CategoryMapping, Long> {
    Optional<CategoryMapping> findByExpenseServiceId(Long expenseServiceId);
    Optional<CategoryMapping> findByReportServiceId(Long reportServiceId);
    
    @Transactional
    void deleteByExpenseServiceId(Long expenseServiceId);
    
    boolean existsByExpenseServiceId(Long expenseServiceId);
}