package com.reports.reports.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "expense_service_id", nullable = false)
    private Long expenseServiceId;
    
    @Column(name = "report_service_id", nullable = false)
    private Long reportServiceId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    public CategoryMapping(Long expenseServiceId, Long reportServiceId, Long userId) {
        this.expenseServiceId = expenseServiceId;
        this.reportServiceId = reportServiceId;
        this.userId = userId;
    }
}