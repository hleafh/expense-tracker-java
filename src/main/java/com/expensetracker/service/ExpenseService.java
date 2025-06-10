package com.expensetracker.service;

import com.expensetracker.entity.Expense;
import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.dto.MonthlyCategoryExpenseDto;
import com.expensetracker.dto.ExpenseCreationResponseDto;
import com.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import com.expensetracker.entity.CategoryBudget;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList; // 新增導入
import java.util.HashMap;   // 新增導入


@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryBudgetService categoryBudgetService; // 注入 CategoryBudgetService

    public ExpenseService(ExpenseRepository expenseRepository, CategoryBudgetService categoryBudgetService) {
        this.expenseRepository = expenseRepository;
        this.categoryBudgetService = categoryBudgetService;
    }

    public ExpenseCreationResponseDto addExpense(ExpenseDto expenseDto) {
        Expense expense = new Expense();
        expense.setTitle(expenseDto.getTitle());
        expense.setAmount(expenseDto.getAmount());
        expense.setCategory(expenseDto.getCategory());
        expense.setDate(expenseDto.getDate());

        Expense savedExpense = expenseRepository.save(expense);

        // 檢查超支邏輯
        CategoryBudgetCheckResult checkResult = checkOverspending(
            expense.getDate().getYear(),
            expense.getDate().getMonthValue(),
            expense.getCategory(),
            expense.getAmount()
        );

        String message = "支出已成功新增。";
        if (checkResult.isOverbudget()) {
            message = String.format("警告：'%s'類別已超出預算！目前總支出 %.2f，預算 %.2f。",
                                    expense.getCategory(),
                                    checkResult.getCurrentCategoryTotalExpense(),
                                    checkResult.getCategoryBudgetAmount());
        }

        return new ExpenseCreationResponseDto(
            savedExpense,
            checkResult.isOverbudget(),
            message,
            checkResult.getCurrentCategoryTotalExpense(),
            checkResult.getCategoryBudgetAmount()
        );
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        expenses.sort(Comparator.comparing(Expense::getDate).reversed());
        return expenses;
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public ExpenseCreationResponseDto updateExpense(Long id, ExpenseDto dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("找不到支出紀錄"));

        Double oldAmount = expense.getAmount(); // 儲存舊金額

        // 更新支出資訊
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDate(dto.getDate());

        Expense updatedExpense = expenseRepository.save(expense);

        // 檢查更新後的超支邏輯
        CategoryBudgetCheckResult checkResult = checkOverspendingForUpdate(
            dto.getDate().getYear(),
            dto.getDate().getMonthValue(),
            dto.getCategory(),
            dto.getAmount(), // 新的金額
            oldAmount // 舊的金額
        );

        String message = "支出已成功更新。";
        if (checkResult.isOverbudget()) {
            message = String.format("警告：'%s'類別更新後超出預算！目前總支出 %.2f，預算 %.2f。",
                                    dto.getCategory(),
                                    checkResult.getCurrentCategoryTotalExpense(),
                                    checkResult.getCategoryBudgetAmount());
        }

        return new ExpenseCreationResponseDto(
            updatedExpense,
            checkResult.isOverbudget(),
            message,
            checkResult.getCurrentCategoryTotalExpense(),
            checkResult.getCategoryBudgetAmount()
        );
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("找不到支出紀錄");
        }
        expenseRepository.deleteById(id);
    }

    public List<MonthlyCategoryExpenseDto> getMonthlyCategoryStatistics(int year, int month) {
        // 1. 獲取所有設定了預算的類別
        List<CategoryBudget> categoryBudgets = categoryBudgetService.getCategoryBudgetsByYearAndMonth(year, month);
        // 將預算轉換為 Map，方便查詢
        Map<String, Double> budgetMap = categoryBudgets.stream()
                .collect(Collectors.toMap(CategoryBudget::getCategory, CategoryBudget::getAmount));

        // 2. 獲取實際支出總額
        List<Object[]> expenseResults = expenseRepository.sumAmountByCategoryAndMonth(year, month);
        // 將支出轉換為 Map，方便查詢
        Map<String, Double> expenseMap = expenseResults.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).doubleValue()
                ));

        List<MonthlyCategoryExpenseDto> statisticsList = new ArrayList<>();

        // 3. 遍歷所有有預算的類別，並查找其對應的實際支出
        for (Map.Entry<String, Double> entry : budgetMap.entrySet()) {
            String category = entry.getKey();
            Double budgetAmount = entry.getValue();
            // 如果該類別有實際支出，則使用實際支出金額；否則為 0.0
            Double totalAmount = expenseMap.getOrDefault(category, 0.0);
            statisticsList.add(new MonthlyCategoryExpenseDto(category, totalAmount, budgetAmount));
        }

        // 4. (可選) 處理只有支出但沒有設定預算的類別
        // 如果您希望沒有預算但有支出的類別也能顯示，可以添加以下邏輯
        for (Map.Entry<String, Double> entry : expenseMap.entrySet()) {
            String category = entry.getKey();
            if (!budgetMap.containsKey(category)) { // 如果這個類別不在預算 Map 中
                statisticsList.add(new MonthlyCategoryExpenseDto(category, entry.getValue(), 0.0)); // 預算為 0
            }
        }

        // 可以根據需要對結果列表進行排序 (例如按類別名稱)
        statisticsList.sort(Comparator.comparing(MonthlyCategoryExpenseDto::getCategory));

        return statisticsList;
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByDateBetween(startDate, endDate);
    }

    // 內部輔助類，用於返回超支檢查的詳細結果
    private static class CategoryBudgetCheckResult {
        private boolean overbudget;
        private Double currentCategoryTotalExpense;
        private Double categoryBudgetAmount;

        public CategoryBudgetCheckResult(boolean overbudget, Double currentCategoryTotalExpense, Double categoryBudgetAmount) {
            this.overbudget = overbudget;
            this.currentCategoryTotalExpense = currentCategoryTotalExpense;
            this.categoryBudgetAmount = categoryBudgetAmount;
        }

        public boolean isOverbudget() { return overbudget; }
        public Double getCurrentCategoryTotalExpense() { return currentCategoryTotalExpense; }
        public Double getCategoryBudgetAmount() { return categoryBudgetAmount; }
    }

    /**
     * 超支檢查範例 (用於新增支出)
     *
     * @param year 年份
     * @param month 月份
     * @param category 支出類別
     * @param newExpenseAmount 本次新增的金額
     * @return 包含超支狀態和相關金額的結果物件
     */
    private CategoryBudgetCheckResult checkOverspending(int year, int month, String category, Double newExpenseAmount) {
        BigDecimal currentCategoryTotalExpenseBd = expenseRepository.sumAmountByYearAndMonthAndCategory(year, month, category);
        double currentCategoryTotalExpense = currentCategoryTotalExpenseBd.doubleValue();

        Optional<CategoryBudget> budget = categoryBudgetService.getCategoryBudgetsByYearAndMonth(year, month).stream()
                .filter(b -> b.getCategory().equals(category))
                .findFirst();

        double categoryBudgetAmount = 0.0;
        if (budget.isPresent()) {
            categoryBudgetAmount = budget.get().getAmount();
        }

        boolean isOverbudget = (currentCategoryTotalExpense + newExpenseAmount > categoryBudgetAmount) && (categoryBudgetAmount > 0);
        return new CategoryBudgetCheckResult(isOverbudget, currentCategoryTotalExpense + newExpenseAmount, categoryBudgetAmount);
    }

    /**
     * 超支檢查範例 (用於更新支出)
     *
     * @param year 年份
     * @param month 月份
     * @param category 支出類別
     * @param newAmount 更新後的金額
     * @param oldAmount 更新前的金額
     * @return 包含超支狀態和相關金額的結果物件
     */
    private CategoryBudgetCheckResult checkOverspendingForUpdate(int year, int month, String category, Double newAmount, Double oldAmount) {
        BigDecimal currentCategoryTotalExpenseBd = expenseRepository.sumAmountByYearAndMonthAndCategory(year, month, category);
        double currentCategoryTotalExpense = currentCategoryTotalExpenseBd.doubleValue();

        // 從當前總支出中減去舊金額，然後加上新金額，得到更新後的實際總支出
        double projectedTotalExpense = currentCategoryTotalExpense - oldAmount + newAmount;

        Optional<CategoryBudget> budget = categoryBudgetService.getCategoryBudgetsByYearAndMonth(year, month).stream()
                .filter(b -> b.getCategory().equals(category))
                .findFirst();

        double categoryBudgetAmount = 0.0;
        if (budget.isPresent()) {
            categoryBudgetAmount = budget.get().getAmount();
        }

        boolean isOverbudget = (projectedTotalExpense > categoryBudgetAmount) && (categoryBudgetAmount > 0);
        return new CategoryBudgetCheckResult(isOverbudget, projectedTotalExpense, categoryBudgetAmount);
    }
}