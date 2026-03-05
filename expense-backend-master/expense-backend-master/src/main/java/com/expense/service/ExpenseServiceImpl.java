package com.expense.service;

import com.expense.exception.InsufficientBalanceException;
import com.expense.model.CategoryExpenseDTO;
import com.expense.model.Expense;
import com.expense.repository.ExpenseRepository;
import com.expense.repository.IncomeRepository;
import com.expense.service.ExpenseService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired 
    private IncomeRepository incomeRepository;

    @Override
    public Expense addExpense(Long userId, Expense expense) {
        log.info("Start addExpense for userId: {}", userId);
        expense.setId(null); // Ensure ID is null for new entity

        Double currentBalance = getAccountBalance(userId);
        if (expense.getAmount() > currentBalance) {
            throw new InsufficientBalanceException("Are you taken a Loan ? please add it in income becouse balance is 0");
        }

        expense.setUserId(userId);
        if (expense.getTimestamp() == null) {
            expense.setTimestamp(LocalDateTime.now());
        }

        Expense savedExpense = expenseRepository.save(expense);

//        Publish Kafka message for CREATE();
        // ExpenseKafkaMessage message = new ExpenseKafkaMessage(
        //         savedExpense.getId(),
        //         savedExpense.getAmount(),
        //         savedExpense.getDescription(),
        //         savedExpense.getTimestamp(),
        //         savedExpense.getUserId(),
        //         savedExpense.getCategory(),
        //         ExpenseKafkaMessage.ActionType.CREATE
        // );
        // expenseProducer.sendExpenseMessage(savedExpense.getId(), message);
        log.info("End addExpense, saved expense id: {}", savedExpense.getId());
        return savedExpense;
    }

    @Override
    public Expense updateExpense(Long expenseId, Expense updatedExpense) {
        log.info("Start updateExpense with id: {}", expenseId);
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Only update non-null fields
        if (updatedExpense.getAmount() != null) {
            expense.setAmount(updatedExpense.getAmount());
        }
        if (updatedExpense.getCategory() != null) {
            expense.setCategory(updatedExpense.getCategory());
        }
        if (updatedExpense.getDescription() != null) {
            expense.setDescription(updatedExpense.getDescription());
        }
        if (updatedExpense.getTimestamp() != null) {
            expense.setTimestamp(updatedExpense.getTimestamp());
        }

        Expense updated = expenseRepository.save(expense);
        
//         Publish Kafka message for UPDATE
        // ExpenseKafkaMessage message = new ExpenseKafkaMessage(
        //         updated.getId(),
        //         updated.getAmount(),
        //         updated.getDescription(),
        //         updated.getTimestamp(),
        //         updated.getUserId(),
        //         updated.getCategory(),
        //         ExpenseKafkaMessage.ActionType.UPDATE
        // );
        // expenseProducer.sendExpenseMessage(updated.getId(), message);

        log.info("End updateExpense for id: {}", expenseId);
        return updated;
    }

    @Override
    public void deleteExpense(Long expenseId) {
        log.info("Start deleteExpense with id: {}", expenseId);
        
        // Publish Kafka message for DELETE
        // ExpenseKafkaMessage message = new ExpenseKafkaMessage();
        // message.setId(expenseId);
        // message.setActionType(ExpenseKafkaMessage.ActionType.DELETE);
        // expenseProducer.sendExpenseMessage(expenseId, message);
        
        expenseRepository.deleteById(expenseId);
        log.info("End deleteExpense with id: {}", expenseId);
    }

    @Override
    public List<Expense> getExpenseHistory(Long userId) {
        log.info("Fetching expense history for userId: {}", userId);
        return expenseRepository.findByUserId(userId);
    }

	/*
	 * @Override public Double getAccountBalance(Long userId) {
	 * log.info("Calculating balance for userId: {}", userId); User user =
	 * userRepository.findById(userId).orElseThrow(() -> new
	 * RuntimeException("User not found"));
	 * 
	 * List<Expense> expenses = expenseRepository.findByUserId(userId); Double
	 * totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
	 * 
	 * Double balance = user.getAccount_balance() - totalSpent;
	 * log.info("User salary: {}, total spent: {}, balance: {}",
	 * user.getAccount_balance(), totalSpent, balance);
	 * 
	 * return balance; }
	 */
    
    public Double getAccountBalance(Long userId) {
        Double totalIncome = incomeRepository.getTotalIncomeByUserId(userId);
        Double totalExpense = expenseRepository.getTotalExpenseByUserId(userId);
        return totalIncome - totalExpense;
    }
    
    
    @Override
    public List<CategoryExpenseDTO> getCategoryWiseSummary(Long userId) {
   log.info("Start: getCategoryWiseSummary for userId: {}", userId);
        List<Object[]> rawData = expenseRepository.getCategoryWiseExpenseSummaryRaw(userId);

        double total = rawData.stream()
            .mapToDouble(obj -> (Double) obj[1])
            .sum();

        List<CategoryExpenseDTO> summary = new ArrayList<>();
        for (Object[] obj : rawData) {
            String categoryName = (String) obj[0];
            Double amount = (Double) obj[1];
            double percent = total == 0 ? 0 : (amount / total) * 100;
            summary.add(new CategoryExpenseDTO(categoryName, amount, Math.round(percent * 100.0) / 100.0)); // Rounded %
        }

   log.info("End: getCategoryWiseSummary for userId: {}, categories: {}", userId, summary.size());
        return summary;
    }
    
    
    
	/*
	 * public List<CategoryExpenseDTO> getCategorySummaryWithPercentage(Long userId,
	 * LocalDateTime start, LocalDateTime end) { List<Object[]> rawData =
	 * expenseRepository.getCategorySummaryByDateRange(userId, start, end);
	 * 
	 * double total = rawData.stream() .mapToDouble(row -> (Double) row[1]) .sum();
	 * 
	 * return rawData.stream() .map(row -> { String category = (String) row[0];
	 * Double amount = (Double) row[1]; double percentage = total > 0 ? (amount *
	 * 100.0) / total : 0.0; return new CategoryExpenseDTO(category, amount,
	 * percentage); }) .collect(Collectors.toList()); }
	 */

    


    public List<CategoryExpenseDTO> getWeeklySummaryWithPercentage(Long userId) {
        log.info("Start: getWeeklySummaryWithPercentage for userId: {}", userId);
        LocalDate today = LocalDate.now();
        LocalDate start = today.with(DayOfWeek.MONDAY);
        LocalDate end = today.with(DayOfWeek.SUNDAY);
        List<CategoryExpenseDTO> result = getCategorySummaryWithPercentage(userId, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        log.info("End: getWeeklySummaryWithPercentage for userId: {}, result size: {}", userId, result.size());
        return result;
    }

    public List<CategoryExpenseDTO> getMonthlySummaryWithPercentage(Long userId) {
        log.info("Start: getMonthlySummaryWithPercentage for userId: {}", userId);
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        List<CategoryExpenseDTO> result = getCategorySummaryWithPercentage(userId, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        log.info("End: getMonthlySummaryWithPercentage for userId: {}, result size: {}", userId, result.size());
        return result;
    }

    public List<CategoryExpenseDTO> getYearlySummaryWithPercentage(Long userId) {
        log.info("Start: getYearlySummaryWithPercentage for userId: {}", userId);
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfYear(1);
        LocalDate end = today.withDayOfYear(today.lengthOfYear());
        List<CategoryExpenseDTO> result = getCategorySummaryWithPercentage(userId, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        log.info("End: getYearlySummaryWithPercentage for userId: {}, result size: {}", userId, result.size());
        return result;
    }

	@Override
	public List<CategoryExpenseDTO> getAllTimeSummaryWithPercentage(Long userId) {
	    log.info("Start: getAllTimeSummaryWithPercentage for userId: {}", userId);
		// TODO Auto-generated method stub
		List<CategoryExpenseDTO> result = getCategorySummaryWithPercentage(userId, null, null);
		log.info("End: getAllTimeSummaryWithPercentage for userId: {}, result size: {}", userId, result.size());
		return result;
	}

    
	public List<CategoryExpenseDTO> getCategorySummaryWithPercentage(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
	    log.info("Start: getCategorySummaryWithPercentage for userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);
        List<Object[]> rawData = expenseRepository.getCategorySummaryByDateRange(userId, startDate, endDate);

        double totalAmount = rawData.stream()
                .mapToDouble(row -> (Double) row[1])
                .sum();

        List<CategoryExpenseDTO> result = rawData.stream().map(row -> {
            String categoryName = (String) row[0];
            Double amount = (Double) row[1];
            double percentage = totalAmount > 0 ? (amount * 100.0) / totalAmount : 0.0;
            return new CategoryExpenseDTO(categoryName, amount, percentage);
        }).collect(Collectors.toList());
        log.info("End: getCategorySummaryWithPercentage for userId: {}, result size: {}", userId, result.size());
        return result;
    }


}
