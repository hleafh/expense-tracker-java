package com.expensetracker.dto;

public class MonthlyCategoryExpenseDto {
    private String category;
    private Double totalAmount;
    private Double budgetAmount; // 新增：預算金額

    // 原始建構子 (保持不變，或可移除，取決於您的使用方式)
    public MonthlyCategoryExpenseDto(String category, Double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.budgetAmount = 0.0; // 預設值，確保即使沒有提供預算也不會是 null
    }

    // 新增一個包含 budgetAmount 的建構子
    public MonthlyCategoryExpenseDto(String category, Double totalAmount, Double budgetAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.budgetAmount = budgetAmount;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getBudgetAmount() { // 新增 Getter
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) { // 新增 Setter
        this.budgetAmount = budgetAmount;
    }
}