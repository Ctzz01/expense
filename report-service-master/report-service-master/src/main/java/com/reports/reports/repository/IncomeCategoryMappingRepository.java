package com.reports.reports.repository;

import com.reports.reports.model.IncomeCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface IncomeCategoryMappingRepository extends JpaRepository<IncomeCategoryMapping, Long> {
    Optional<IncomeCategoryMapping> findByExpenseServiceId(Long expenseServiceId);
    Optional<IncomeCategoryMapping> findByReportServiceId(Long reportServiceId);
    
    @Transactional
    void deleteByExpenseServiceId(Long expenseServiceId);
    
    boolean existsByExpenseServiceId(Long expenseServiceId);
}