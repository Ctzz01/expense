package com.expense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.expense.model.Income;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserId(Long userId);
    
    boolean existsByUserIdAndCategory_Id(Long userId, Long categoryId);
    
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId")
    Double getTotalIncomeByUserId(Long userId);
}
