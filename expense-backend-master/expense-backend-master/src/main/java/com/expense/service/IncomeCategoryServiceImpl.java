package com.expense.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.expense.model.IncomeCategory;
import com.expense.repository.IncomeCategoryRepository;
import com.expense.repository.IncomeRepository;

@Service
public class IncomeCategoryServiceImpl implements IncomeCategoryService {

    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;

    @Autowired
    private IncomeRepository incomeRepository;

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

        IncomeCategory savedCategory = incomeCategoryRepository.save(category);
        
        // Publish Kafka message for CREATE
        // IncomeCategoryKafkaMessage message = new IncomeCategoryKafkaMessage(
        //         savedCategory.getId(),
        //         savedCategory.getName(),
        //         savedCategory.getUserId(),
        //         IncomeCategoryKafkaMessage.ActionType.CREATE
        // );
        
        // incomeCategoryProducer.sendMessage(savedCategory.getUserId(), message);
        return savedCategory;
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

        boolean hasIncomes = incomeRepository.existsByUserIdAndCategory_Id(userId, categoryId);
        if (hasIncomes) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Income or Expense exists for this category"
            );
        }

        // Publish Kafka message for DELETE before deleting
        // IncomeCategoryKafkaMessage message = new IncomeCategoryKafkaMessage(
        //         category.getId(),
        //         category.getName(),
        //         category.getUserId(),
        //         IncomeCategoryKafkaMessage.ActionType.DELETE
        // );
        // 
        // incomeCategoryProducer.sendMessage(category.getUserId(), message);
        incomeCategoryRepository.delete(category);
    }
}
