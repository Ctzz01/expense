package com.expense.model;

public class CategoryExpenseDTO {
    private String categoryName;
    private Double totalAmount;
    private Double percentage; // New field

    public CategoryExpenseDTO(String categoryName, Double totalAmount, Double percentage) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
