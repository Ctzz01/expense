package com.expense.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expense.model.Expense;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    
    List<Expense> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    boolean existsByUserIdAndCategory_Id(Long userId, Long categoryId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId")
    Double getTotalExpenseByUserId(Long userId);
    
    
    @Query("SELECT e.category.name, SUM(e.amount) " +
    	       "FROM Expense e WHERE e.userId = :userId GROUP BY e.category.name")
    	List<Object[]> getCategoryWiseExpenseSummaryRaw(@Param("userId") Long userId);

    	@Query("SELECT e.category.name, SUM(e.amount) " +
    		       "FROM Expense e WHERE e.userId = :userId AND e.timestamp BETWEEN :startDate AND :endDate " +
    		       "GROUP BY e.category.name")
    		List<Object[]> getCategorySummaryByDateRange(@Param("userId") Long userId,
    		                                             @Param("startDate") LocalDateTime startDate,
    		                                             @Param("endDate") LocalDateTime endDate);

    	
    	
    }
