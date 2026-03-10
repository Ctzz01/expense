package com.expense.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.expense.model.IncomeCategory;
import com.expense.service.IncomeCategoryService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/income-category")
@Slf4j
public class IncomeCategoryController {

    @Autowired
    private IncomeCategoryService categoryService;

    @PostMapping
    public ResponseEntity<IncomeCategory> createOrUpdate(@RequestHeader("X-User-Id") Long userId,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) Long id) {
        if (id != null && name != null) {
            // Update operation
            log.info("Start: update IncomeCategory id={}, userId={}, newCategoryName={}", id, userId, name);
            IncomeCategory category = new IncomeCategory();
            category.setName(name);
            IncomeCategory updatedCategory = categoryService.updateCategory(id, category, userId);
            log.info("End: updated IncomeCategory id={} for userId={}", id, userId);
            return ResponseEntity.ok(updatedCategory);
        } else if (name != null) {
            // Create operation
            log.info("Start: create IncomeCategory for userId={}, categoryName={}", userId, name);
            IncomeCategory createdCategory = categoryService.createCategory(userId, name);
            log.info("End: created IncomeCategory with id={}", createdCategory.getId());
            return ResponseEntity.ok(createdCategory);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<IncomeCategory>> list(@RequestHeader("X-User-Id") Long userId) {
        log.info("Start: get all IncomeCategories for userId={}", userId);
        List<IncomeCategory> categories = categoryService.getAllCategoriesForUser(userId);
        log.info("End: retrieved {} IncomeCategories for userId={}", categories.size(), userId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeCategory> update(@PathVariable Long id, @Valid @RequestBody IncomeCategory category, @RequestHeader("X-User-Id") Long userId) {
        log.info("Start: update IncomeCategory id={}, userId={}, newCategoryName={}", id, userId, category.getName());
        IncomeCategory updatedCategory = categoryService.updateCategory(id, category, userId);
        log.info("End: updated IncomeCategory id={} for userId={}", id, userId);
        return ResponseEntity.ok(updatedCategory);
    }
    
    // Temporary POST endpoint for updates (workaround for PUT mapping issue)
    @PostMapping("/update")
    public ResponseEntity<IncomeCategory> updateViaPost(@RequestParam Long id, @Valid @RequestBody IncomeCategory category, @RequestHeader("X-User-Id") Long userId) {
        log.info("Start: update IncomeCategory via POST id={}, userId={}, newCategoryName={}", id, userId, category.getName());
        IncomeCategory updatedCategory = categoryService.updateCategory(id, category, userId);
        log.info("End: updated IncomeCategory via POST id={} for userId={}", id, userId);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id,
                                         @RequestHeader("X-User-Id") Long userId) {
        log.info("Start: delete IncomeCategory with id={}, for userId={}", id, userId);
        categoryService.deleteCategory(id, userId);
        log.info("End: IncomeCategory with id={} deleted successfully for userId={}", id, userId);
        return ResponseEntity.ok("Category deleted");
    }
}

