package com.expensetracker.dto.response;

import com.expensetracker.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String title,
        BigDecimal amount,
        Category category,
        LocalDate expenseDate,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
