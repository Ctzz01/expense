package com.expense.kafka;

import com.expense.dto.ExpenseKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseProducer {
    @Value("${app.kafka.expense-topic}") // read from property file
    private String topic;

    private final KafkaTemplate<Long, ExpenseKafkaMessage> expenseKafkaTemplate;

    public void sendExpenseMessage(Long key, ExpenseKafkaMessage message) {
        log.info("Producing Kafka message to topic {}: {}", topic, message);
        expenseKafkaTemplate.send(topic, key, message);
    }
}