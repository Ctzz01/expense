package com.aifinancial.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class FinancialSummary {
    private Long userId;
    private LocalDate analysisDate;
    private Integer totalTransactions;
    private Integer monthsOfData;
    
    // Income data
    private Double monthlyIncome;
    private Double totalIncome;
    private List<IncomeCategoryBreakdown> incomeBreakdown;
    
    // Expense data
    private Double monthlyExpense;
    private Double totalExpense;
    private List<ExpenseCategoryBreakdown> expenseBreakdown;
    
    // Financial metrics
    private Double savingsRate;
    private Double netSavings;
    private Double averageMonthlySavings;
    
    // Trends
    private String expenseTrend;
    private Map<String, Double> monthlyComparison;
    
    @Data
    public static class IncomeCategoryBreakdown {
        private String category;
        private Double amount;
        private Double percentage;
    }
    
    @Data
    public static class ExpenseCategoryBreakdown {
        private String category;
        private Double amount;
        private Double percentage;
    }
}
