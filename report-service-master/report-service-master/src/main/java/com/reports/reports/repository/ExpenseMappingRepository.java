package com.reports.reports.repository;

import com.reports.reports.model.ExpenseMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ExpenseMappingRepository extends JpaRepository<ExpenseMapping, Long> {
    Optional<ExpenseMapping> findByExpenseServiceId(Long expenseServiceId);
    Optional<ExpenseMapping> findByReportServiceId(Long reportServiceId);
    
    @Transactional
    void deleteByExpenseServiceId(Long expenseServiceId);
    
    boolean existsByExpenseServiceId(Long expenseServiceId);
}