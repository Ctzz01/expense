package com.reports.reports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.reports.reports.model.Income;
import com.reports.reports.model.IncomeCategory;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserId(Long userId);
    
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId")
    Double getTotalIncomeByUserId(Long userId);
}

