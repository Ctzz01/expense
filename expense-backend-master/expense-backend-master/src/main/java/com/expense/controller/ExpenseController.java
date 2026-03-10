package com.expense.controller;

import com.expense.exception.InsufficientBalanceException;
import com.expense.model.Category;
import com.expense.model.CategoryExpenseDTO;
import com.expense.model.Expense;
import com.expense.repository.CategoryRepository;
import com.expense.service.CategoryService;
import com.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

	@Autowired
	private ExpenseService expenseService;
	@Autowired
	private  CategoryService categoryService;
    @Autowired
	private CategoryRepository categoryRepository;
	
	@PostMapping("/addExpense")
	public ResponseEntity<String> addExpense(@RequestHeader("X-User-Id") Long userId, @RequestBody @Valid Expense expense) {
	    log.info("Start: addExpense for userId={}, category={}", userId, expense.getCategory().getName());

	    try {
	        expense.setUserId(userId);
	        if (expense.getTimestamp() == null) {
	            expense.setTimestamp(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
	        }

	        String categoryName = expense.getCategory().getName().trim().toLowerCase();

	        // Try to fetch category (user-defined or system)
	        Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCaseAndUserId(categoryName, userId)
	                .or(() -> categoryRepository.findByNameIgnoreCaseAndUserIdIsNull(categoryName));

	        if (existingCategory.isPresent()) {
	            expense.setCategory(existingCategory.get());
	        } else {
	            // Create user-defined category if not found
	            Category newCategory = new Category();
	            newCategory.setName(categoryName);
	            newCategory.setUserId(userId);
	            Category savedCategory = categoryRepository.save(newCategory);
	            expense.setCategory(savedCategory);
	        }
             // Below is for kafka integration to add expense for report servi
	        expenseService.addExpense(userId, expense);

	        return ResponseEntity.ok("Expense added successfully!");
	    } catch (InsufficientBalanceException e) {
	        log.warn("Insufficient balance for userId={}: {}", userId, e.getMessage());
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}
	
	
    @PutMapping("/updateExpense/{expenseId}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long expenseId, @RequestBody Expense expense) {
        log.info("Start: updateExpense with id={}, new amount={}", expenseId, expense.getAmount());
        Expense updatedExpense = expenseService.updateExpense(expenseId, expense);
        log.info("End: updated Expense with id={}", updatedExpense.getId());
        return ResponseEntity.ok(updatedExpense);
    }
    
    @DeleteMapping("/deleteExpense/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }


	@GetMapping("/getBalance")
	public ResponseEntity<?> getBalance(@RequestHeader("X-User-Id") Long userId) {
		log.info("Start: getBalance for userId={}", userId);

		double balance = expenseService.getAccountBalance(userId);
		log.info("Balance retrieved for userId={}, balance={}", userId, balance);
		log.info("End: getBalance for userId={}", userId);
		return ResponseEntity.ok(balance);
	}

	@GetMapping("/getExpenseHistory")
	public ResponseEntity<?> getExpenseHistory(
			@RequestHeader("X-User-Id") Long userId,
			@RequestParam(required = false) String filter,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		log.info("Start: getExpenseHistory for userId={}, filter={}, startDate={}, endDate={}", userId, filter, startDate, endDate);

		List<Expense> history = expenseService.getExpenseHistory(userId, filter, startDate, endDate);
		log.info("Retrieved {} expenses for userId={}", history.size(), userId);
		log.info("End: getExpenseHistory for userId={}", userId);
		return ResponseEntity.ok(history);
	}
	
	
//deffault
	/*
	 * @GetMapping("/category-summary/{userId}") public ResponseEntity<?>
	 * getCategoryWiseExpenseSummary(@PathVariable Long userId) {
	 * log.info("Start: getCategoryWiseExpenseSummary for userId={}", userId);
	 * 
	 * if (!userRepository.existsById(userId)) {
	 * log.warn("User with ID {} doesn't exist", userId); return
	 * ResponseEntity.badRequest().body("User doesn't exist"); }
	 * 
	 * List<CategoryExpenseDTO> summary =
	 * expenseService.getCategoryWiseSummary(userId);
	 * 
	 * log.info("End: getCategoryWiseExpenseSummary for userId={}, categories={}",
	 * userId, summary.size()); return ResponseEntity.ok(summary); }
	 */
	
	
	@GetMapping("/getCategoryWiseSummary")
	public ResponseEntity<?> getCategoryWiseSummary(
	        @RequestHeader("X-User-Id") Long userId,
	        @RequestParam(required = false) String filter,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		log.info("getCategoryWiseSummary", userId);
	    List<CategoryExpenseDTO> result;

	    switch (filter != null ? filter.toLowerCase() : "") {
	        case "week":
	            result = expenseService.getWeeklySummaryWithPercentage(userId);
	            break;
	        case "month":
	            result = expenseService.getMonthlySummaryWithPercentage(userId);
	            break;
	        case "year":
	            result = expenseService.getYearlySummaryWithPercentage(userId);
	            break;
	        default:
	            if (startDate != null && endDate != null) {
	                result = expenseService.getCategorySummaryWithPercentage(userId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
	            } else {
	                result = expenseService.getAllTimeSummaryWithPercentage(userId);
	            }
	    }

	    return ResponseEntity.ok(result);
	}

	
}
