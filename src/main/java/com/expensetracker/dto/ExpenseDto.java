// src/main/java/com/expensetracker/dto/ExpenseDto.java
package com.expensetracker.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ExpenseDto {

    @NotBlank(message = "支出標題不能為空")
    private String title;

    @NotNull(message = "支出金額不能為空")
    @Positive(message = "支出金額必須為正數")
    private Double amount;

    @NotBlank(message = "支出類別不能為空")
    private String category;

    @NotNull(message = "支出日期不能為空")
    private LocalDate date;

    public ExpenseDto() {
    }

    public ExpenseDto(String title, Double amount, String category, LocalDate date) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}