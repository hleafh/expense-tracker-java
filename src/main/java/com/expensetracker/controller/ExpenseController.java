package com.expensetracker.controller;

import com.expensetracker.entity.Expense;
import com.expensetracker.dto.ExpenseDto;
import com.expensetracker.dto.MonthlyCategoryExpenseDto;
import com.expensetracker.dto.ExpenseCreationResponseDto;
import com.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // 新增支出
    @PostMapping
    public ResponseEntity<ExpenseCreationResponseDto> addExpense(@Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseCreationResponseDto response = expenseService.addExpense(expenseDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 取得單筆支出
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        Optional<Expense> expense = expenseService.getExpenseById(id);
        return expense.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                      .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 更新支出
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDto expenseDto) {
        try {
            ExpenseCreationResponseDto response = expenseService.updateExpense(id, expenseDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "fail", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "更新失敗：" + e.getMessage()));
        }
    }

    // 刪除支出
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "支出已刪除"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "fail", "message", e.getMessage()));
        }
    }

    // 取得指定年月分類支出統計 (無需修改，因為 Service 層返回的 DTO 已更新)
    @GetMapping("/statistics/{year}/{month}")
    public ResponseEntity<List<MonthlyCategoryExpenseDto>> getMonthlyCategoryStatistics(
                @PathVariable int year,
                @PathVariable int month) {
        List<MonthlyCategoryExpenseDto> statistics = expenseService.getMonthlyCategoryStatistics(year, month);
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpensesByDateRange(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Expense> expenses;
        if (startDate != null && endDate != null) {
            expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        } else {
            expenses = expenseService.getAllExpenses();
        }
        return ResponseEntity.ok(expenses);
    }
}