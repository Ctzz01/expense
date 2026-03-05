package com.expense.dto;


import com.expense.dto.IncomeKafkaMessage.ActionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryKafkaMessage {
	
	
    private Long id;

    private String name;
    
    private Long userId;
    private ActionType actionType;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

}
