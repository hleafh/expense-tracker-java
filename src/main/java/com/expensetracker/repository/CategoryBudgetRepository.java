// src/main/java/com/expensetracker/repository/CategoryBudgetRepository.java
package com.expensetracker.repository;

import com.expensetracker.entity.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    // 根據年份、月份和類別查詢單個分類預算
    Optional<CategoryBudget> findByYearAndMonthAndCategory(Integer year, Integer month, String category);

    // 查詢指定年份和月份的所有分類預算
    List<CategoryBudget> findByYearAndMonth(Integer year, Integer month);
}