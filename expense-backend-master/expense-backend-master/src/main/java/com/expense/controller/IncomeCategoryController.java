package com.expense.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<IncomeCategory> create(@RequestParam Long userId,
                                                 @RequestParam String name) {
        log.info("Start: create IncomeCategory for userId={}, categoryName={}", userId, name);
        IncomeCategory createdCategory = categoryService.createCategory(userId, name);
        log.info("End: created IncomeCategory with id={}", createdCategory.getId());
        return ResponseEntity.ok(createdCategory);
    }

    @GetMapping
    public ResponseEntity<List<IncomeCategory>> list(@RequestParam Long userId) {
        log.info("Start: get all IncomeCategories for userId={}", userId);
        List<IncomeCategory> categories = categoryService.getAllCategoriesForUser(userId);
        log.info("End: retrieved {} IncomeCategories for userId={}", categories.size(), userId);
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id,
                                         @RequestParam Long userId) {
        log.info("Start: delete IncomeCategory with id={}, for userId={}", id, userId);
        categoryService.deleteCategory(id, userId);
        log.info("End: IncomeCategory with id={} deleted successfully for userId={}", id, userId);
        return ResponseEntity.ok("Category deleted");
    }
}

