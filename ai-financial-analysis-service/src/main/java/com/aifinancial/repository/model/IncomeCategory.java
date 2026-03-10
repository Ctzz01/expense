package com.aifinancial.repository.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "income_category")
@Data
public class IncomeCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long userId;
}
