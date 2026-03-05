package com.reports.reports.repository;

import com.reports.reports.model.IncomeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface IncomeMappingRepository extends JpaRepository<IncomeMapping, Long> {
    Optional<IncomeMapping> findByExpenseServiceId(Long expenseServiceId);
    Optional<IncomeMapping> findByReportServiceId(Long reportServiceId);
    
    @Transactional
    void deleteByExpenseServiceId(Long expenseServiceId);
    
    boolean existsByExpenseServiceId(Long expenseServiceId);
}