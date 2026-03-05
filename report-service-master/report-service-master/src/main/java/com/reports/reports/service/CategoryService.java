package com.reports.reports.service;


import java.util.List;
import java.util.Optional;

import com.reports.reports.model.Category;

public interface CategoryService {

    Category createCategory(Category category, Long userId);

    Optional<Category> getAllCategoriesForUser(Long userId);

    Category getCategoryById(Long id, Long userId);
    
    Category getCategoryByName(String name, Long userId);

    Category updateCategory(Long id, Category updatedCategory, Long userId);

    void deleteCategory(Long id, Long userId);

	Category createCategoryIfNotExist(Category category, Long userId);

}
