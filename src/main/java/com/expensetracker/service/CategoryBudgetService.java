package com.expensetracker.service;

import com.expensetracker.dto.CategoryBudgetDto;
import com.expensetracker.entity.CategoryBudget;
import com.expensetracker.repository.CategoryBudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryBudgetService {

    private final CategoryBudgetRepository categoryBudgetRepository;

    public CategoryBudgetService(CategoryBudgetRepository categoryBudgetRepository) {
        this.categoryBudgetRepository = categoryBudgetRepository;
    }

    // 新增或更新分類預算
    public CategoryBudget addOrUpdateCategoryBudget(CategoryBudgetDto dto) {
        Optional<CategoryBudget> existingBudget = categoryBudgetRepository.findByYearAndMonthAndCategory(
                dto.getYear(), dto.getMonth(), dto.getCategory());

        CategoryBudget categoryBudget;
        if (existingBudget.isPresent()) {
            categoryBudget = existingBudget.get();
            categoryBudget.setAmount(dto.getAmount());
        } else {
            categoryBudget = new CategoryBudget(dto.getYear(), dto.getMonth(), dto.getCategory(), dto.getAmount());
        }
        return categoryBudgetRepository.save(categoryBudget);
    }

    // 查詢指定年份和月份的所有分類預算
    public List<CategoryBudget> getCategoryBudgetsByYearAndMonth(int year, int month) {
        return categoryBudgetRepository.findByYearAndMonth(year, month);
    }

    // 計算指定年份和月份的總預算
    public Double calculateTotalMonthlyBudget(int year, int month) {
        List<CategoryBudget> budgets = categoryBudgetRepository.findByYearAndMonth(year, month);
        return budgets.stream()
                .mapToDouble(CategoryBudget::getAmount)
                .sum();
    }
}