package com.expense.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expense.dto.CategoryKafkaMessage;
import com.expense.dto.ExpenseKafkaMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryProducer {

	@Value("${app.kafka.category-topic}")
	private String topic;
	
	private final KafkaTemplate<Long, CategoryKafkaMessage> categoryKaftemplate;
	
	public void sendMessage(Long key, CategoryKafkaMessage categoryMsg) {
		 log.info("Producing Kafka message to topic {}: {}", topic, categoryMsg);
		 categoryKaftemplate.send(topic, key, categoryMsg);
	}

}
