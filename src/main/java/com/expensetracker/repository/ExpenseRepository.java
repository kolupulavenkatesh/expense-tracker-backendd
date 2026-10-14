package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE (:from IS NULL OR e.expenseDate >= :from) AND (:to IS NULL OR e.expenseDate <= :to) GROUP BY e.category")
    List<Object[]> sumAmountByCategoryBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE (:from IS NULL OR e.expenseDate >= :from) AND (:to IS NULL OR e.expenseDate <= :to)")
    BigDecimal sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(e) FROM Expense e WHERE (:from IS NULL OR e.expenseDate >= :from) AND (:to IS NULL OR e.expenseDate <= :to)")
    long countBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate >= :monthStart AND e.expenseDate <= :monthEnd")
    BigDecimal sumAmountForCurrentMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

    List<Expense> findByCategory(Category category);
}
