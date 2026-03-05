package com.expense.repository;

import com.expense.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Check if a category exists with the same name for a specific user
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    // Check if a category exists with the same name globally (user is null)
    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);

    // Fetch all categories visible to a user: common (user=null) + user-specific
    List<Category> findByUserIdIsNullOrUserId(Long userId);

	Optional<Category> findByName(String id);
	
	//Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);
	Optional<Category> findByNameIgnoreCaseAndUserIdIsNull(String name);
	//boolean existsByIdIgnoreCaseAndUser(Long id, User user);
	Optional<Category> findByNameIgnoreCaseAndUserId(String categoryName, Long userId);
}
