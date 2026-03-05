package com.reports.reports.kafka;

import com.reports.reports.dto.ExpenseKafkaMessage;
import com.reports.reports.dto.ExpenseKafkaMessage.ActionType;
import com.reports.reports.model.Expense;
import com.reports.reports.model.Category;
import com.reports.reports.model.ExpenseMapping;
import com.reports.reports.repository.CategoryRepository;
import com.reports.reports.repository.ExpenseRepository;
import com.reports.reports.repository.ExpenseMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseConsumer {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMappingRepository expenseMappingRepository;

    @KafkaListener(
        topics = "${app.kafka.expense-topic}", 
        groupId = "${spring.kafka.consumer.group-id}", 
        containerFactory = "expenseKafkaListenerFactory",
        idIsGroup = false,
        id = "expenseConsumer",
        concurrency = "1"
    )
    public void listenExpenseEvents(
            @Payload ExpenseKafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.info("Received Expense Kafka Message from topic: {}, partition: {}, offset: {}, message: {}", 
                    topic, partition, offset, message);

            ActionType actionType = message.getActionType();

            if (actionType == null) {
                log.warn("ActionType is null for message: {}", message);
                return;
            }

            switch (actionType) {
                case CREATE:
                case UPDATE:
                    handleCreateOrUpdate(message);
                    break;
                case DELETE:
                    handleDelete(message);
                    break;
                default:
                    log.warn("Unknown ActionType: {} for message: {}", actionType, message);
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset, e);
        }
    }

    private void handleCreateOrUpdate(ExpenseKafkaMessage message) {
        try {
            Category kafkaCategory = message.getCategory();
            Category managedCategory = null;

            if (kafkaCategory != null) {
                // First try to find by name and userId to avoid ID conflicts
                Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCaseAndUserId(kafkaCategory.getName(), kafkaCategory.getUserId());
                if (existingCategory.isPresent()) {
                    managedCategory = existingCategory.get();
                } else {
                    // Create new category without setting ID (let it be auto-generated)
                    managedCategory = new Category();
                    managedCategory.setName(kafkaCategory.getName());
                    managedCategory.setUserId(kafkaCategory.getUserId());
                    managedCategory = categoryRepository.save(managedCategory);
                }
            }

            // Check if we already have a mapping for this expense service ID
            Optional<ExpenseMapping> existingMapping = expenseMappingRepository.findByExpenseServiceId(message.getId());
            Expense expense;
            
            if (existingMapping.isPresent()) {
                // Update existing expense
                Long reportServiceId = existingMapping.get().getReportServiceId();
                expense = expenseRepository.findById(reportServiceId).orElse(new Expense());
            } else {
                // Create new expense
                expense = new Expense();
            }
            
            expense.setAmount(message.getAmount());
            expense.setDescription(message.getDescription());
            expense.setTimestamp(message.getTimestamp());
            expense.setUserId(message.getUserId());
            expense.setCategory(managedCategory);
            
            // Save the expense
            Expense savedExpense = expenseRepository.save(expense);
            
            // Create or update mapping if it's a new expense
            if (!existingMapping.isPresent()) {
                ExpenseMapping mapping = new ExpenseMapping(message.getId(), savedExpense.getId(), message.getUserId());
                expenseMappingRepository.save(mapping);
            }
            
            log.info("Expense saved/updated successfully: {}", savedExpense);
        } catch (DataAccessException e) {
            log.error("Database error while saving/updating expense: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing expense message: {}", message.getId(), e);
        }
    }

    private void handleDelete(ExpenseKafkaMessage message) {
        try {
            if (message.getId() == null) {
                log.error("Cannot delete expense with null ID");
                return;
            }
            
            // Find the mapping for this expense service ID
            Optional<ExpenseMapping> mapping = expenseMappingRepository.findByExpenseServiceId(message.getId());
            if (mapping.isPresent()) {
                Long reportServiceId = mapping.get().getReportServiceId();
                
                // Delete the expense from report service
                if (expenseRepository.existsById(reportServiceId)) {
                    expenseRepository.deleteById(reportServiceId);
                    log.info("Expense deleted successfully with report service ID: {} (expense service ID: {})", reportServiceId, message.getId());
                }
                
                // Delete the mapping
                expenseMappingRepository.deleteByExpenseServiceId(message.getId());
                log.info("Expense mapping deleted for expense service ID: {}", message.getId());
            } else {
                log.warn("No mapping found for expense service ID: {}", message.getId());
            }
        } catch (DataAccessException e) {
            log.error("Database error while deleting expense: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting expense: {}", message.getId(), e);
        }
    }
}