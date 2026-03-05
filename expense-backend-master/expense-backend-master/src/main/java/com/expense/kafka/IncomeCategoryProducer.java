package com.expense.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expense.dto.IncomeCategoryKafkaMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeCategoryProducer {

    @Value("${app.kafka.income-category-topic}")
    private String topic;
    
    private final KafkaTemplate<Long, IncomeCategoryKafkaMessage> incomeCategoryKafkaTemplate;
    
    public void sendMessage(Long key, IncomeCategoryKafkaMessage incomeCategoryMsg) {
        log.info("Producing Kafka message to topic {}: {}", topic, incomeCategoryMsg);
        incomeCategoryKafkaTemplate.send(topic, key, incomeCategoryMsg);
    }
}