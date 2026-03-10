package com.aifinancial.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "income")
@Data
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String description;

    private LocalDateTime date;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private IncomeCategory category;
}
