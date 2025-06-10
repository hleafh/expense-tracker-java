// src/main/java/com/expensetracker/dto/CategoryBudgetDto.java
package com.expensetracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryBudgetDto {

    @NotNull(message = "年份不能為空")
    private Integer year;

    @NotNull(message = "月份不能為空")
    @Min(value = 1, message = "月份需介於1到12")
    private Integer month;

    @NotBlank(message = "類別不能為空")
    private String category;

    @NotNull(message = "預算金額不能為空")
    @Min(value = 0, message = "預算金額不能小於0")
    private Double amount;

    public CategoryBudgetDto() {}

    public CategoryBudgetDto(Integer year, Integer month, String category, Double amount) {
        this.year = year;
        this.month = month;
        this.category = category;
        this.amount = amount;
    }

    // Getters and Setters
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}