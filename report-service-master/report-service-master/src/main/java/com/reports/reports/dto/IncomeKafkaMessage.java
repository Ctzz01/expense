
package com.reports.reports.dto;

import com.reports.reports.model.IncomeCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeKafkaMessage {
    private Long id;
    private Double amount;
    private String description;
    private LocalDateTime date;
    private Long userId;
    private IncomeCategory categoryId;
    
    @JsonProperty("actionType")
    private ActionType actionType;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}
