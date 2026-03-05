package com.expense.service;

import com.expense.model.Category;
import com.expense.model.CategoryExpenseDTO;
import com.expense.repository.CategoryRepository;
import com.expense.repository.ExpenseRepository;
import com.expense.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Category createCategory(Category category, Long userId) {
        category.setId(null); // Ensure ID is null for new entity
        String normalizedName = category.getName().trim().toLowerCase();

        boolean existsForUser = categoryRepository.existsByNameIgnoreCaseAndUserId(normalizedName, userId);
        boolean existsAsGlobal = categoryRepository.existsByNameIgnoreCaseAndUserIdIsNull(normalizedName);

        if (existsForUser || existsAsGlobal) {
            throw new IllegalArgumentException("Category already exists");
        }

        category.setName(normalizedName);
        category.setUserId(userId);
        Category SaveCategory = categoryRepository.save(category);
        
        // CategoryKafkaMessage categoryMsg = new CategoryKafkaMessage(
        //         SaveCategory.getId(),
        //         SaveCategory.getName(),
        //         SaveCategory.getUserId(),
        //         CategoryKafkaMessage.ActionType.CREATE
        // );
        
        // categoryProducer.sendMessage(SaveCategory.getUserId(),categoryMsg);
        return SaveCategory;
    }
    

 


	/*
	 * @Override public Category createCategoryIfNotExist(Category category, String
	 * username) { User user = userRepository.findByUsername(username)
	 * .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
	 * "User not found"));
	 * 
	 * boolean existsForUser =
	 * categoryRepository.existsByIdIgnoreCaseAndUser(category.getId(), user);
	 * boolean existsAsGlobal =
	 * categoryRepository.existsByIdIgnoreCaseAndUserIsNull(category.getId());
	 * 
	 * if (existsForUser || existsAsGlobal) { throw new
	 * IllegalArgumentException("Category already exists"); }
	 * 
	 * category.setName(normalizedName); category.setUser(user); return
	 * categoryRepository.save(category); }
	 */
    @Override
    public List<Category> getAllCategoriesForUser(Long userId) {
        return categoryRepository.findByUserIdIsNullOrUserId(userId);
    }

    @Override
    public Category getCategoryById(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (category.getUserId() != null && !category.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to category");
        }

        return category;
    }
    
    public Category getCategoryByName(String name, Long userId) {
        String normalizedName = name.trim().toLowerCase();

        return categoryRepository.findByNameIgnoreCaseAndUserId(normalizedName, userId)
                .or(() -> categoryRepository.findByNameIgnoreCaseAndUserIdIsNull(normalizedName))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }
    
    @Override
    public Category updateCategory(Long id, Category updatedCategory, Long userId) {
        Category existing = getCategoryById(id, userId); // already checks ownership

        existing.setName(updatedCategory.getName().trim().toLowerCase());
        Category savedCategory = categoryRepository.save(existing);
        
        // Publish Kafka message for UPDATE
        // CategoryKafkaMessage categoryMsg = new CategoryKafkaMessage(
        //         savedCategory.getId(),
        //         savedCategory.getName(),
        //         savedCategory.getUserId(),
        //         CategoryKafkaMessage.ActionType.UPDATE
        // );
        
        // categoryProducer.sendMessage(savedCategory.getUserId(), categoryMsg);
        return savedCategory;
    }

    @Override
    public void deleteCategory(Long id, Long userId) {
        Category category = getCategoryById(id, userId); // already checks ownership

        if (category.getUserId() == null) {
            throw new UnsupportedOperationException("Cannot delete a common category");
        }

        boolean hasExpenses = expenseRepository.existsByUserIdAndCategory_Id(userId, id);
        if (hasExpenses) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Expense exists for this category"
            );
        }

        // Publish Kafka message for DELETE before deleting
        // CategoryKafkaMessage categoryMsg = new CategoryKafkaMessage(
        //         category.getId(),
        //         category.getName(),
        //         category.getUserId(),
        //         CategoryKafkaMessage.ActionType.DELETE
        // );
        
        // categoryProducer.sendMessage(category.getUserId(), categoryMsg);
        categoryRepository.delete(category);
    }

	@Override
	public Category createCategoryIfNotExist(Category category, Long userId) {
		// TODO Auto-generated method stub
		return null;
	}
}
