package com.reports.reports.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reports.reports.model.Income;
import com.reports.reports.model.IncomeCategory;
import com.reports.reports.repository.IncomeCategoryRepository;
import com.reports.reports.repository.IncomeRepository;


@Service
public class IncomeService {

    
    private IncomeRepository incomeRepository;
   
    
    private IncomeCategoryRepository incomeCategoryRepository;

	/*
	 * public Income addIncome(String username, Income income) { User user =
	 * userRepository.findByUsername(username) .orElseThrow(() -> new
	 * RuntimeException("User not found"));
	 * 
	 * income.setUser(user); if (income.getDate() == null) {
	 * income.setDate(LocalDateTime.now()); }
	 * 
	 * return incomeRepository.save(income); }
	 */

    public Income addIncome(Long userId, Income income) {
        income.setId(null); // Ensure ID is null for new entity
        income.setUserId(userId);

        // Set current date if not provided
        if (income.getDate() == null) {
            income.setDate(LocalDateTime.now());
        }

        // Handle null category or missing name
        if (income.getCategoryId() == null || income.getCategoryId().getName() == null) {
            throw new RuntimeException("Income category name must be provided.");
        }

        // Normalize category name
        String categoryName = income.getCategoryId().getName().trim().toLowerCase();

        // Check if the category already exists for the user
        IncomeCategory incomeCategory = incomeCategoryRepository
                .findByNameIgnoreCaseAndUserId(categoryName, userId)
                .orElse(null);

        // If category doesn't exist, create new
        if (incomeCategory == null) {
            incomeCategory = new IncomeCategory();
            incomeCategory.setName(categoryName);
            incomeCategory.setUserId(userId);
            incomeCategory = incomeCategoryRepository.save(incomeCategory);
        }

        // Set the (existing or new) category
        income.setCategoryId(incomeCategory);

        // Save and return the income
        return incomeRepository.save(income);
    }

    
    
    public List<Income> getIncomes(Long userId, String filter, LocalDate start, LocalDate end) {
        // Default: month
        LocalDateTime from;
        LocalDateTime to;
        if (start != null && end != null) {
            from = start.atStartOfDay();
            to = end.atTime(LocalTime.MAX);
        } else if ("today".equalsIgnoreCase(filter)) {
            LocalDate today = LocalDate.now();
            from = today.atStartOfDay();
            to = today.atTime(LocalTime.MAX);
        } else if ("week".equalsIgnoreCase(filter)) {
            LocalDate today = LocalDate.now();
            from = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            to = today.with(java.time.DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
        } else if ("year".equalsIgnoreCase(filter)) {
            LocalDate today = LocalDate.now();
            from = today.withDayOfYear(1).atStartOfDay();
            to = today.withDayOfYear(today.lengthOfYear()).atTime(LocalTime.MAX);
        } else { // month (default)
            LocalDate today = LocalDate.now();
            from = today.withDayOfMonth(1).atStartOfDay();
            to = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
        }

        // Fallback to repository filtering in memory
        return incomeRepository.findByUserId(userId).stream()
        	    .filter(i -> i.getDate() != null && !i.getDate().isBefore(from) && !i.getDate().isAfter(to))
        	    .collect(Collectors.toList());

    }

    public void deleteIncome(Long incomeId) {
        incomeRepository.deleteById(incomeId);
    }

    public Income updateIncome(Long id, Income newIncome) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        income.setAmount(newIncome.getAmount());
        income.setDescription(newIncome.getDescription());
        income.setDate(newIncome.getDate());

        return incomeRepository.save(income);
    }
}
