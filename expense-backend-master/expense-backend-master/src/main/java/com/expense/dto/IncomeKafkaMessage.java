package com.expense.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.expense.model.IncomeCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeKafkaMessage {
    private Long id;
    private Double amount;
    private String description;
    private LocalDateTime date;
    private Long userId;
    private IncomeCategory categoryId;
    private ActionType actionType;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}


