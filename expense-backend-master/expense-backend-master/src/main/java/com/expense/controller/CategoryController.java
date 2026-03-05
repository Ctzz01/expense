package com.expense.controller;

import com.expense.model.Category;
import com.expense.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    // GET all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(@RequestParam Long userId) {
        log.info("Start: getAllCategories for userId={}", userId);
        List<Category> categories = categoryService.getAllCategoriesForUser(userId);
        log.info("End: getAllCategories for userId={}, totalCategories={}", userId, categories.size());
        return ResponseEntity.ok(categories);
    }

    // POST create a category
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category, @RequestParam Long userId) {
        log.info("Start: createCategory for userId={}, categoryName={}", userId, category.getName());
        Category created = categoryService.createCategory(category, userId);
        log.info("End: createCategory for userId={}, createdCategoryId={}", userId, created.getId());
        return ResponseEntity.ok(created);
    }

    // PUT update a category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category, @RequestParam Long userId) {
        log.info("Start: updateCategory id={}, userId={}, newCategoryName={}", id, userId, category.getName());
        Category updated = categoryService.updateCategory(id, category, userId);
        log.info("End: updateCategory id={}, userId={}", id, userId);
        return ResponseEntity.ok(updated);
    }

    // DELETE a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @RequestParam Long userId) {
        log.info("Start: deleteCategory id={}, userId={}", id, userId);
        categoryService.deleteCategory(id, userId);
        log.info("End: deleteCategory id={}, userId={}", id, userId);
        return ResponseEntity.noContent().build();
    }
}
