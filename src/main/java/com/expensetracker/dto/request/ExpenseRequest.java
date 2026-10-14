package com.expensetracker.dto.request;

import com.expensetracker.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {
}
