package com.expense.service;

import com.expense.model.CategoryExpenseDTO;
import com.expense.model.Expense;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {

    Expense addExpense(Long userId, Expense expense);

    Expense updateExpense(Long expenseId, Expense updatedExpense);

    void deleteExpense(Long expenseId);

    List<Expense> getExpenseHistory(Long userId);

    List<Expense> getExpenseHistory(Long userId, String filter, LocalDate startDate, LocalDate endDate);

    Double getAccountBalance(Long userId);

	List<CategoryExpenseDTO> getCategoryWiseSummary(Long userId);

	List<CategoryExpenseDTO> getWeeklySummaryWithPercentage(Long userId);

	List<CategoryExpenseDTO> getMonthlySummaryWithPercentage(Long userId);

	List<CategoryExpenseDTO> getYearlySummaryWithPercentage(Long userId);

	List<CategoryExpenseDTO> getAllTimeSummaryWithPercentage(Long userId);

	List<CategoryExpenseDTO> getCategorySummaryWithPercentage(Long userId, LocalDateTime atStartOfDay,
			LocalDateTime atTime);
}
