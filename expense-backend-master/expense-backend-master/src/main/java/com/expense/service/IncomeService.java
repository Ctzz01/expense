package com.expense.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expense.model.Income;
import com.expense.model.IncomeCategory;
import com.expense.repository.IncomeCategoryRepository;
import com.expense.repository.IncomeRepository;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;
   
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;


    public Income addIncome(Long userId, Income income) {
        income.setId(null); // Ensure ID is null for new entity
        income.setUserId(userId);

        // Set current date if not provided
        if (income.getDate() == null) {
            income.setDate(LocalDateTime.now());
        }

        // Handle null category or missing name
        if (income.getCategory() == null || income.getCategory().getName() == null) {
            throw new RuntimeException("Income category name must be provided.");
        }

        // Normalize category name
        String categoryName = income.getCategory().getName().trim().toLowerCase();

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
        income.setCategory(incomeCategory);

        // Save and return the income
        Income savedIncome = incomeRepository.save(income);

        // Publish Kafka message
//        IncomeKafkaMessage message = new IncomeKafkaMessage(
//                savedIncome.getId(),
//                savedIncome.getAmount(),
//                savedIncome.getDescription(),
//                savedIncome.getDate(),
//                savedIncome.getUserId(),
//                savedIncome.getCategory(),
//                IncomeKafkaMessage.ActionType.CREATE
//        );
//        incomeProducer.sendIncomeMessage(savedIncome.getId(), message);

        return savedIncome;
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
public List<Income> getTotalIncome(Long userId) {

        return incomeRepository.findByUserId(userId);

}
    	public void deleteIncome(Long incomeId) {
		incomeRepository.deleteById(incomeId);
		// Publish Kafka message for deletion
		// IncomeKafkaMessage message = new IncomeKafkaMessage();
		// message.setId(incomeId);
		// message.setActionType(IncomeKafkaMessage.ActionType.DELETE);
		// incomeProducer.sendIncomeMessage(incomeId, message);
	}

    public Income updateIncome(Long id, Income newIncome) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        // Only update non-null fields
        if (newIncome.getAmount() != null) {
            income.setAmount(newIncome.getAmount());
        }
        if (newIncome.getDescription() != null) {
            income.setDescription(newIncome.getDescription());
        }
        if (newIncome.getDate() != null) {
            income.setDate(newIncome.getDate());
        }
        
        // Handle category update if provided
        if (newIncome.getCategory() != null && newIncome.getCategory().getName() != null) {
            String categoryName = newIncome.getCategory().getName().trim().toLowerCase();
            IncomeCategory incomeCategory = incomeCategoryRepository
                    .findByNameIgnoreCaseAndUserId(categoryName, income.getUserId())
                    .orElseGet(() -> {
                        IncomeCategory newCategory = new IncomeCategory();
                        newCategory.setName(categoryName);
                        newCategory.setUserId(income.getUserId());
                        return incomeCategoryRepository.save(newCategory);
                    });
            income.setCategory(incomeCategory);
        }

        Income updatedIncome = incomeRepository.save(income);

		// Publish Kafka message for update
		// IncomeKafkaMessage message = new IncomeKafkaMessage(
		// 		updatedIncome.getId(),
		// 		updatedIncome.getAmount(),
		// 		updatedIncome.getDescription(),
		// 		updatedIncome.getDate(),
		// 		updatedIncome.getUserId(),
		// 		updatedIncome.getCategory(),
		// 		IncomeKafkaMessage.ActionType.UPDATE
		// );
		// incomeProducer.sendIncomeMessage(updatedIncome.getId(), message);

		return updatedIncome;
	}
}
