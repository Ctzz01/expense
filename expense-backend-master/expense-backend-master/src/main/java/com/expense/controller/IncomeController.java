package com.expense.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.format.annotation.DateTimeFormat;

import com.expense.dto.IncomeKafkaMessage;
import com.expense.kafka.IncomeProducer;
import com.expense.model.Income;
import com.expense.service.IncomeService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/income")
@Slf4j
public class IncomeController {

    @Autowired
    private IncomeService incomeService;
    
    @Autowired
    private IncomeProducer incomeProducer;


    @PostMapping("/addIncome")
    public ResponseEntity<Income> addIncome(@RequestHeader("X-User-Id") Long userId, @RequestBody @Valid Income income) {
        log.info("Start: addIncome for userId={}, amount={}", userId, income.getAmount());
        Income savedIncome = incomeService.addIncome(userId, income);
       // incomeKafkaMessage.setSource(savedIncome.getSource());
        // Send Kafka message
//        IncomeKafkaMessage kafkaMessage = new IncomeKafkaMessage(
//        	    savedIncome.getId(),
//        	    savedIncome.getAmount(),
//        	    savedIncome.getDescription(),
//        	    savedIncome.getDate(),
//        	    userId,
//        	    savedIncome.getCategory(),
//                IncomeKafkaMessage.ActionType.CREATE
//        	);
//        incomeProducer.sendIncomeMessage(savedIncome.getId(), kafkaMessage);
        log.info("End: added Income with id={}", savedIncome.getId());
        return ResponseEntity.ok(savedIncome);
    }

    
    
    
    
    @GetMapping("/getIncomes")
    public ResponseEntity<List<Income>> getIncomes(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        log.info("Start: getIncomes for userId={}, filter={}, startDate={}, endDate={}", userId, filter, startDate, endDate);
        List<Income> incomes = incomeService.getIncomes(userId, filter, startDate, endDate);
        log.info("End: retrieved {} incomes for userId={}", incomes.size(), userId);
        return ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/deleteIncome/{incomeId}")
    public ResponseEntity<String> deleteIncome(@PathVariable Long incomeId) {
        log.info("Start: deleteIncome with id={}", incomeId);
        incomeService.deleteIncome(incomeId);
        log.info("End: Income with id={} deleted successfully", incomeId);
        return ResponseEntity.ok("Income deleted successfully.");
    }

    @PutMapping("/updateIncome/{incomeId}")
    public ResponseEntity<Income> updateIncome(@PathVariable Long incomeId, @RequestBody Income income) {
        log.info("Start: updateIncome with id={}, new amount={}", incomeId, income.getAmount());
        Income updatedIncome = incomeService.updateIncome(incomeId, income);
        log.info("End: updated Income with id={}", updatedIncome.getId());
        return ResponseEntity.ok(updatedIncome);
    }
}

