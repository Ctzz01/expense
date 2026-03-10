package com.aifinancial.repository;

import com.aifinancial.repository.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    
    List<Income> findByUserId(Long userId);
    
    List<Income> findByUserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId AND i.date BETWEEN :startDate AND :endDate")
    Double getTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i.category.name, SUM(i.amount) " +
           "FROM Income i WHERE i.userId = :userId AND i.date BETWEEN :startDate AND :endDate " +
           "GROUP BY i.category.name")
    List<Object[]> getCategorySummaryByDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
