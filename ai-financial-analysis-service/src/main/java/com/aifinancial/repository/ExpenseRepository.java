package com.aifinancial.repository;

import com.aifinancial.repository.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByUserId(Long userId);
    
    List<Expense> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId AND e.timestamp BETWEEN :startDate AND :endDate")
    Double getTotalExpenseByUserIdAndDateRange(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.category.name, SUM(e.amount) " +
           "FROM Expense e WHERE e.userId = :userId AND e.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category.name")
    List<Object[]> getCategorySummaryByDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}
