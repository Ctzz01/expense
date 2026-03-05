package com.reports.reports.kafka;

import com.reports.reports.dto.IncomeKafkaMessage;
import com.reports.reports.dto.IncomeKafkaMessage.ActionType;
import com.reports.reports.model.Income;
import com.reports.reports.model.IncomeCategory;
import com.reports.reports.model.IncomeMapping;
import com.reports.reports.repository.IncomeCategoryRepository;
import com.reports.reports.repository.IncomeRepository;
import com.reports.reports.repository.IncomeMappingRepository;
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
public class IncomeConsumer {

    private final IncomeRepository incomeRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final IncomeMappingRepository incomeMappingRepository;

    @KafkaListener(
        topics = "${app.kafka.income-topic}", 
        groupId = "${spring.kafka.consumer.group-id}", 
        containerFactory = "incomeKafkaListenerFactory",
        idIsGroup = false,
        id = "incomeConsumer",
        concurrency = "1"
    )
    public void listenIncomeEvents(
            @Payload IncomeKafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.info("Received Income Kafka Message from topic: {}, partition: {}, offset: {}, message: {}", 
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

    private void handleCreateOrUpdate(IncomeKafkaMessage message) {
        try {
            IncomeCategory kafkaCategory = message.getCategoryId();
            IncomeCategory managedCategory = null;

            if (kafkaCategory != null) {
                // First try to find by name and userId to avoid ID conflicts
                Optional<IncomeCategory> existingCategory = incomeCategoryRepository.findByNameIgnoreCaseAndUserId(kafkaCategory.getName(), kafkaCategory.getUserId());
                if (existingCategory.isPresent()) {
                    managedCategory = existingCategory.get();
                } else {
                    // Create new category without setting ID (let it be auto-generated)
                    managedCategory = new IncomeCategory();
                    managedCategory.setName(kafkaCategory.getName());
                    managedCategory.setUserId(kafkaCategory.getUserId());
                    managedCategory = incomeCategoryRepository.save(managedCategory);
                }
            }

            // Check if we already have a mapping for this expense service ID
            Optional<IncomeMapping> existingMapping = incomeMappingRepository.findByExpenseServiceId(message.getId());
            Income income;
            
            if (existingMapping.isPresent()) {
                // Update existing income
                Long reportServiceId = existingMapping.get().getReportServiceId();
                income = incomeRepository.findById(reportServiceId).orElse(new Income());
            } else {
                // Create new income
                income = new Income();
            }
            
            income.setAmount(message.getAmount());
            income.setDescription(message.getDescription());
            income.setDate(message.getDate());
            income.setUserId(message.getUserId());
            income.setCategoryId(managedCategory);
            
            // Save the income
            Income savedIncome = incomeRepository.save(income);
            
            // Create or update mapping if it's a new income
            if (!existingMapping.isPresent()) {
                IncomeMapping mapping = new IncomeMapping(message.getId(), savedIncome.getId(), message.getUserId());
                incomeMappingRepository.save(mapping);
            }
            log.info("Income saved/updated successfully: {}", income);
        } catch (DataAccessException e) {
            log.error("Database error while saving/updating income: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing income message: {}", message.getId(), e);
        }
    }

    private void handleDelete(IncomeKafkaMessage message) {
        try {
            if (message.getId() == null) {
                log.error("Cannot delete income with null ID");
                return;
            }
            
            // Find the mapping for this expense service ID
            Optional<IncomeMapping> mapping = incomeMappingRepository.findByExpenseServiceId(message.getId());
            if (mapping.isPresent()) {
                Long reportServiceId = mapping.get().getReportServiceId();
                
                // Delete the income from report service
                if (incomeRepository.existsById(reportServiceId)) {
                    incomeRepository.deleteById(reportServiceId);
                    log.info("Income deleted successfully with report service ID: {} (expense service ID: {})", reportServiceId, message.getId());
                }
                
                // Delete the mapping
                incomeMappingRepository.deleteByExpenseServiceId(message.getId());
                log.info("Income mapping deleted for expense service ID: {}", message.getId());
            } else {
                log.warn("No mapping found for expense service ID: {}", message.getId());
            }
        } catch (DataAccessException e) {
            log.error("Database error while deleting income: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting income: {}", message.getId(), e);
        }
    }
}
