package com.expense.kafka;


import com.expense.dto.IncomeKafkaMessage;
import com.expense.dto.IncomeKafkaMessage.ActionType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeProducer {
    @Value("${app.kafka.income-topic}") // read from property file
    private String topic;

    private final KafkaTemplate<Long, IncomeKafkaMessage> incomeKafkaTemplate;

    public void sendIncomeMessage(Long key, IncomeKafkaMessage message) {
        log.info("Producing Kafka message to topic {}: {}", topic, message);
        incomeKafkaTemplate.send(topic, key, message);
    }
}
