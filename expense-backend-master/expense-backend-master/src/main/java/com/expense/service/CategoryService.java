package com.expense.service;

import com.expense.model.Category;
import com.expense.model.CategoryExpenseDTO;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category, Long userId);

    List<Category> getAllCategoriesForUser(Long userId);

    Category getCategoryById(Long id, Long userId);
    
    Category getCategoryByName(String name, Long userId);

    Category updateCategory(Long id, Category updatedCategory, Long userId);

    void deleteCategory(Long id, Long userId);

	Category createCategoryIfNotExist(Category category, Long userId);

}
