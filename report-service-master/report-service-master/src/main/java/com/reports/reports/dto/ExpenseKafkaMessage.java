package com.reports.reports.dto;

import com.reports.reports.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseKafkaMessage {
    private Long id;
    private Double amount;
    private String description;
    private LocalDateTime timestamp;
    private Long userId;
    private Category category;
    
    @JsonProperty("actionType")
    private ActionType actionType;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}