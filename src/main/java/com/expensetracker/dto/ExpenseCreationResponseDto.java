package com.expensetracker.dto;

import com.expensetracker.entity.Expense;

public class ExpenseCreationResponseDto {
    private Expense expense;
    private boolean overbudget;
    private String message;
    private Double currentCategoryTotalExpense; // 可以選擇性返回當前類別總支出
    private Double categoryBudgetAmount;        // 可以選擇性返回類別預算

    public ExpenseCreationResponseDto(Expense expense, boolean overbudget, String message) {
        this.expense = expense;
        this.overbudget = overbudget;
        this.message = message;
    }

    public ExpenseCreationResponseDto(Expense expense, boolean overbudget, String message, Double currentCategoryTotalExpense, Double categoryBudgetAmount) {
        this.expense = expense;
        this.overbudget = overbudget;
        this.message = message;
        this.currentCategoryTotalExpense = currentCategoryTotalExpense;
        this.categoryBudgetAmount = categoryBudgetAmount;
    }

    // Getters and Setters
    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public boolean isOverbudget() {
        return overbudget;
    }

    public void setOverbudget(boolean overbudget) {
        this.overbudget = overbudget;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getCurrentCategoryTotalExpense() {
        return currentCategoryTotalExpense;
    }

    public void setCurrentCategoryTotalExpense(Double currentCategoryTotalExpense) {
        this.currentCategoryTotalExpense = currentCategoryTotalExpense;
    }

    public Double getCategoryBudgetAmount() {
        return categoryBudgetAmount;
    }

    public void setCategoryBudgetAmount(Double categoryBudgetAmount) {
        this.categoryBudgetAmount = categoryBudgetAmount;
    }
}