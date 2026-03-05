package com.expense.dto;


import java.time.LocalDateTime;

import com.expense.model.IncomeCategory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDTO {
    private Long id;
    private Double amount;
    private String description;
    private LocalDateTime date;
    private Long userId;
    private IncomeCategory categoryId;
}

