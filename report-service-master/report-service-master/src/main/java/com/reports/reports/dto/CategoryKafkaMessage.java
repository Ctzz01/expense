package com.reports.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reports.reports.dto.IncomeKafkaMessage.ActionType;

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
    @JsonProperty("actionType")
    private ActionType actionType;
    
    public enum ActionType{
    	CREATE,
    	UPDATE,
    	DELETE
    }

	
	
}
