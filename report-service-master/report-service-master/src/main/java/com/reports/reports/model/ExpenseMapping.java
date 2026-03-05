package com.reports.reports.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expense_mapping", uniqueConstraints = {
    @UniqueConstraint(columnNames = "expenseServiceId")
})
public class ExpenseMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long expenseServiceId;  // ID from expense-service
    private Long reportServiceId;   // ID in report-service
    private Long userId;
    
    public ExpenseMapping(Long expenseServiceId, Long reportServiceId, Long userId) {
        this.expenseServiceId = expenseServiceId;
        this.reportServiceId = reportServiceId;
        this.userId = userId;
    }
}