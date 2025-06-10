// src/main/java/com/expensetracker/controller/CategoryBudgetController.java
package com.expensetracker.controller;

import com.expensetracker.dto.CategoryBudgetDto;
import com.expensetracker.entity.CategoryBudget;
import com.expensetracker.service.CategoryBudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category-budgets")
public class CategoryBudgetController {

    private final CategoryBudgetService categoryBudgetService;

    public CategoryBudgetController(CategoryBudgetService categoryBudgetService) {
        this.categoryBudgetService = categoryBudgetService;
    }

    // 新增或更新分類預算
    @PostMapping
    public ResponseEntity<CategoryBudget> addOrUpdateCategoryBudget(@Valid @RequestBody CategoryBudgetDto budgetDto) {
        CategoryBudget categoryBudget = categoryBudgetService.addOrUpdateCategoryBudget(budgetDto);
        return new ResponseEntity<>(categoryBudget, HttpStatus.CREATED);
    }

    // 查詢指定年份和月份的所有分類預算
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<CategoryBudget>> getCategoryBudgets(@PathVariable int year, @PathVariable int month) {
        List<CategoryBudget> budgets = categoryBudgetService.getCategoryBudgetsByYearAndMonth(year, month);
        return new ResponseEntity<>(budgets, HttpStatus.OK);
    }

    // 查詢指定年份和月份的總預算
    @GetMapping("/total/{year}/{month}")
    public ResponseEntity<Map<String, Double>> getTotalMonthlyBudget(@PathVariable int year, @PathVariable int month) {
        Double totalBudget = categoryBudgetService.calculateTotalMonthlyBudget(year, month);
        return new ResponseEntity<>(Map.of("totalBudget", totalBudget), HttpStatus.OK);
    }
}