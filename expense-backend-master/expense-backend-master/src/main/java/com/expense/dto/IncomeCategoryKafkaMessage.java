package com.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeCategoryKafkaMessage {
    private Long id;
    private String name;
    private Long userId;
    private ActionType actionType;
    
    public enum ActionType {
        CREATE, UPDATE, DELETE
    }
    
   
}