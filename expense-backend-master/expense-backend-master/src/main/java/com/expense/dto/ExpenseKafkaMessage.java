package com.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.expense.model.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseKafkaMessage {
    private Long id;
    private Double amount;
    private String description;
    private LocalDateTime timestamp;
    private Long userId;
    private Category category;
    private ActionType actionType;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}