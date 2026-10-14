package com.expensetracker.service;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.SummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.mappers.ExpenseMapper;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.ExpenseSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = expenseMapper.toEntity(request);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponse(saved);
    }

    @Override
    public List<ExpenseResponse> getExpenses(String search, Category category, LocalDate from, LocalDate to) {
        Sort sort = Sort.by(Sort.Order.desc("expenseDate"), Sort.Order.desc("id"));
        Specification<Expense> spec = ExpenseSpecifications.withFilters(search, category, from, to);
        return expenseRepository.findAll(spec, sort)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expenseMapper.updateEntity(expense, request);
        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expenseRepository.delete(expense);
    }

    @Override
    public SummaryResponse getSummary(LocalDate from, LocalDate to) {
        BigDecimal totalAmount = expenseRepository.sumAmountBetween(from, to);
        long count = expenseRepository.countBetween(from, to);

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        BigDecimal currentMonthTotal = expenseRepository.sumAmountForCurrentMonth(monthStart, monthEnd);

        // Populate breakdown with all categories defaulted to zero
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Category cat : Category.values()) {
            breakdown.put(cat.name(), BigDecimal.ZERO);
        }

        List<Object[]> results = expenseRepository.sumAmountByCategoryBetween(from, to);
        for (Object[] row : results) {
            Category cat = (Category) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            if (cat != null && sum != null) {
                breakdown.put(cat.name(), sum);
            }
        }

        return new SummaryResponse(
                totalAmount != null ? totalAmount : BigDecimal.ZERO,
                count,
                currentMonthTotal != null ? currentMonthTotal : BigDecimal.ZERO,
                breakdown
        );
    }
}
