package com.reports.reports.kafka;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.reports.reports.dto.CategoryKafkaMessage;
import com.reports.reports.dto.CategoryKafkaMessage.ActionType;
import com.reports.reports.model.Category;
import com.reports.reports.model.CategoryMapping;
import com.reports.reports.repository.CategoryRepository;
import com.reports.reports.repository.CategoryMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryConsumer {

	private final CategoryRepository categoryRepository;
	private final CategoryMappingRepository categoryMappingRepository;

	@KafkaListener(topics = "${app.kafka.category-topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "categoryKafkaListenerFactory", idIsGroup = false, id = "categoryConsumer", concurrency = "1")
	public void listenCategoryEvents(@Payload CategoryKafkaMessage message,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset) {

		try {
			log.info("Received Category Kafka Message from topic: {}, partition: {}, offset: {}, message: {}", topic,
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
			log.error("Error processing Kafka message from topic: {}, partition: {}, offset: {}", topic, partition,
					offset, e);
		}
	}

	private void handleCreateOrUpdate(CategoryKafkaMessage message) {
		try {
			Category category = null;

			// Check if we already have a mapping for this expense service category ID
			Optional<CategoryMapping> existingMapping = categoryMappingRepository.findByExpenseServiceId(message.getId());

			if (existingMapping.isPresent()) {
				// Update existing category
				Long reportServiceId = existingMapping.get().getReportServiceId();
				category = categoryRepository.findById(reportServiceId).orElse(new Category());
			} else {
				// Create new category
				category = new Category();
			}

			category.setName(message.getName());
			category.setUserId(message.getUserId());

			// Save the category
			Category savedCategory = categoryRepository.save(category);

			// Create mapping if it's a new category
			if (!existingMapping.isPresent()) {
				CategoryMapping mapping = new CategoryMapping(message.getId(), savedCategory.getId(), message.getUserId());
				categoryMappingRepository.save(mapping);
			}
			log.info("Category saved/updated successfully: {}", savedCategory);
		} catch (DataAccessException e) {
			log.error("Database error while saving/updating category: {}", message.getId(), e);
		} catch (Exception e) {
			log.error("Unexpected error while processing category message: {}", message.getId(), e);
		}
	}

	private void handleDelete(CategoryKafkaMessage message) {
		try {
			// Find the mapping for this expense service category ID
			Optional<CategoryMapping> existingMapping = categoryMappingRepository.findByExpenseServiceId(message.getId());

			if (existingMapping.isPresent()) {
				Long reportServiceId = existingMapping.get().getReportServiceId();
				
				// Delete the category
				categoryRepository.deleteById(reportServiceId);
				
				// Delete the mapping
				categoryMappingRepository.delete(existingMapping.get());
				
				log.info("Category deleted successfully for expense service ID: {}", message.getId());
			} else {
				log.warn("No mapping found for expense service category ID: {}", message.getId());
			}
		} catch (DataAccessException e) {
			log.error("Database error while deleting category: {}", message.getId(), e);
		} catch (Exception e) {
			log.error("Unexpected error while deleting category: {}", message.getId(), e);
		}
	}
}
