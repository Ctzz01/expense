package com.reports.reports.service;

import java.util.List;

import com.reports.reports.model.IncomeCategory;


public interface IncomeCategoryService {
    
    IncomeCategory createCategory(Long userId, String categoryName);

    List<IncomeCategory> getAllCategoriesForUser(Long userId);

    IncomeCategory getCategoryByName(String categoryName, Long userId);

    void deleteCategory(Long categoryId, Long userId);
}

