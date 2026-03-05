package com.expense.service;

import java.util.List;

import com.expense.model.IncomeCategory;

public interface IncomeCategoryService {
    
    IncomeCategory createCategory(Long userId, String categoryName);

    List<IncomeCategory> getAllCategoriesForUser(Long userId);

    IncomeCategory getCategoryByName(String categoryName, Long userId);

    void deleteCategory(Long categoryId, Long userId);
}

