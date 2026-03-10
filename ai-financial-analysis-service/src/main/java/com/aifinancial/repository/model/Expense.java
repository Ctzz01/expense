package com.aifinancial.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "expense")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String description;

    private LocalDateTime timestamp;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
