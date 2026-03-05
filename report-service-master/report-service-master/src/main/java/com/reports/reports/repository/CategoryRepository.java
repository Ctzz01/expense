package com.reports.reports.repository;

import com.reports.reports.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
    boolean existsByNameIgnoreCaseAndUserIdIsNull(String name);
    Optional<Category> findByNameIgnoreCaseAndUserId(String name, Long userId);
    Optional<Category> findByNameIgnoreCaseAndUserIdIsNull(String name);
    List<Category> findByUserId(Long userId);
	//List<Category> findByUserIdIsNullOrUserId(Long userId);
}