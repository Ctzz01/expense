package com.reports.reports.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reports.reports.model.IncomeCategory;
import com.reports.reports.repository.IncomeCategoryRepository;


@Service
public class IncomeCategoryServiceImpl implements IncomeCategoryService {

    
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;

    @Override
    public IncomeCategory createCategory(Long userId, String categoryName) {
        String normalizedName = categoryName.trim().toLowerCase();

        boolean existsForUser = incomeCategoryRepository.existsByNameIgnoreCaseAndUserId(normalizedName, userId);
        boolean existsGlobally = incomeCategoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(normalizedName);

        if (existsForUser || existsGlobally) {
            throw new IllegalArgumentException("Category already exists");
        }

        IncomeCategory category = new IncomeCategory();
        category.setName(normalizedName);
        category.setUserId(userId);

        return incomeCategoryRepository.save(category);
    }

    @Override
    public List<IncomeCategory> getAllCategoriesForUser(Long userId) {
        return incomeCategoryRepository.findAll()
                .stream()
                .filter(c -> c.getUserId() == null || c.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public IncomeCategory getCategoryByName(String categoryName, Long userId) {
        String normalizedName = categoryName.trim().toLowerCase();

        return incomeCategoryRepository.findByNameIgnoreCaseAndUserId(normalizedName, userId)
            .or(() -> incomeCategoryRepository.findByNameIgnoreCaseAndUserIdIsNull(normalizedName))
            .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void deleteCategory(Long categoryId, Long userId) {
        IncomeCategory category = incomeCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getUserId() == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot delete system-defined or other user's category");
        }

        incomeCategoryRepository.delete(category);
    }
}

