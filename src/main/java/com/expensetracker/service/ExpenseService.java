package com.expensetracker.service;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.SummaryResponse;
import com.expensetracker.entity.Category;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(ExpenseRequest request);

    List<ExpenseResponse> getExpenses(String search, Category category, LocalDate from, LocalDate to);

    ExpenseResponse getExpenseById(Long id);

    ExpenseResponse updateExpense(Long id, ExpenseRequest request);

    void deleteExpense(Long id);

    SummaryResponse getSummary(LocalDate from, LocalDate to);
}
