// src/main/java/com/expensetracker/repository/ExpenseRepository.java
package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal sumAmountByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month GROUP BY e.category")
    List<Object[]> sumAmountByCategoryAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT e FROM Expense e WHERE e.date >= :startDate AND e.date <= :endDate")
    List<Expense> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 新增：查詢指定年份、月份、類別的總支出
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month AND e.category = :category")
    BigDecimal sumAmountByYearAndMonthAndCategory(@Param("year") int year, @Param("month") int month, @Param("category") String category);
}