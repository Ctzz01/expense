package com.expense.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expense.dto.IncomeKafkaMessage;

@Service
public class KafkaMessageProducer {

	@Autowired
	private KafkaTemplate<Long, IncomeKafkaMessage> kafkaTemplate;

	@Value("${app.kafka.income-topic}")
	private String topic;

	public void sendMessage(IncomeKafkaMessage message) {
		// kafkaTemplate.send(topic, message.getId(), message);
	}

	


}
