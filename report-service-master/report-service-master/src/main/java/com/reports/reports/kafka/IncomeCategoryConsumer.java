package com.reports.reports.kafka;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.reports.reports.dto.IncomeCategoryKafkaMessage;
import com.reports.reports.dto.IncomeCategoryKafkaMessage.ActionType;
import com.reports.reports.model.IncomeCategory;
import com.reports.reports.model.IncomeCategoryMapping;
import com.reports.reports.repository.IncomeCategoryRepository;
import com.reports.reports.repository.IncomeCategoryMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomeCategoryConsumer {

    private final IncomeCategoryRepository incomeCategoryRepository;
    private final IncomeCategoryMappingRepository incomeCategoryMappingRepository;

    @KafkaListener(topics = "${app.kafka.income-category-topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "incomeCategoryKafkaListenerFactory", idIsGroup = false, id = "incomeCategoryConsumer", concurrency = "1")
    public void listenIncomeCategoryEvents(@Payload IncomeCategoryKafkaMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            log.info("Received IncomeCategory Kafka Message from topic: {}, partition: {}, offset: {}, message: {}", topic,
                    partition, offset, message);

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
            log.error("Error processing IncomeCategory Kafka message: {}", message, e);
        }
    }

    private void handleCreateOrUpdate(IncomeCategoryKafkaMessage message) {
        try {
            IncomeCategory incomeCategory = null;

            // Check if we already have a mapping for this expense service income category ID
            Optional<IncomeCategoryMapping> existingMapping = incomeCategoryMappingRepository.findByExpenseServiceId(message.getId());

            if (existingMapping.isPresent()) {
                // Update existing income category
                Long reportServiceId = existingMapping.get().getReportServiceId();
                incomeCategory = incomeCategoryRepository.findById(reportServiceId).orElse(new IncomeCategory());
            } else {
                // Create new income category
                incomeCategory = new IncomeCategory();
            }

            incomeCategory.setName(message.getName());
            incomeCategory.setUserId(message.getUserId());

            // Save the income category
            IncomeCategory savedIncomeCategory = incomeCategoryRepository.save(incomeCategory);

            // Create mapping if it's a new income category
            if (!existingMapping.isPresent()) {
                IncomeCategoryMapping mapping = new IncomeCategoryMapping(message.getId(), savedIncomeCategory.getId(), message.getUserId());
                incomeCategoryMappingRepository.save(mapping);
            }
            log.info("IncomeCategory saved/updated successfully: {}", savedIncomeCategory);
        } catch (DataAccessException e) {
            log.error("Database error while saving/updating income category: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing income category message: {}", message.getId(), e);
        }
    }

    private void handleDelete(IncomeCategoryKafkaMessage message) {
        try {
            // Find the mapping for this expense service income category ID
            Optional<IncomeCategoryMapping> existingMapping = incomeCategoryMappingRepository.findByExpenseServiceId(message.getId());

            if (existingMapping.isPresent()) {
                Long reportServiceId = existingMapping.get().getReportServiceId();
                
                // Delete the income category
                incomeCategoryRepository.deleteById(reportServiceId);
                
                // Delete the mapping
                incomeCategoryMappingRepository.delete(existingMapping.get());
                
                log.info("IncomeCategory deleted successfully for expense service ID: {}", message.getId());
            } else {
                log.warn("No mapping found for expense service income category ID: {}", message.getId());
            }
        } catch (DataAccessException e) {
            log.error("Database error while deleting income category: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting income category: {}", message.getId(), e);
        }
    }
}