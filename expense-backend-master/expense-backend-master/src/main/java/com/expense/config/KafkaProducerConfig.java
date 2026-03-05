package com.expense.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.expense.dto.IncomeKafkaMessage;
import com.expense.dto.CategoryKafkaMessage;
import com.expense.dto.ExpenseKafkaMessage;
import com.expense.dto.IncomeCategoryKafkaMessage;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Income Producer Configuration
    @Bean
    public ProducerFactory<Long, IncomeKafkaMessage> incomeProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<Long, IncomeKafkaMessage> incomeKafkaTemplate() {
        return new KafkaTemplate<>(incomeProducerFactory());
    }
    
    // Expense Producer Configuration
    @Bean
    public ProducerFactory<Long, ExpenseKafkaMessage> expenseProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<Long, ExpenseKafkaMessage> expenseKafkaTemplate() {
        return new KafkaTemplate<>(expenseProducerFactory());
    }
    
    @Bean
    public ProducerFactory<Long,CategoryKafkaMessage> categoryProducerFactory(){
    	Map<String,Object> config = new HashMap<>();
    	config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    	config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,LongSerializer.class);
    	config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
		return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<Long,CategoryKafkaMessage> categoryKafkaTemplate(){
    	return new KafkaTemplate<>(categoryProducerFactory());
    }
    
    // IncomeCategory Producer Configuration
    @Bean
    public ProducerFactory<Long, IncomeCategoryKafkaMessage> incomeCategoryProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<Long, IncomeCategoryKafkaMessage> incomeCategoryKafkaTemplate() {
        return new KafkaTemplate<>(incomeCategoryProducerFactory());
    }
}
