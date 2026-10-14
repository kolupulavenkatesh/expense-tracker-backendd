package com.expensetracker.mappers;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest request) {
        if (request == null) {
            return null;
        }
        Expense expense = new Expense();
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description() != null ? request.description().trim() : null);
        return expense;
    }

    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) {
            return null;
        }
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getExpenseDate(),
                expense.getDescription(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    public void updateEntity(Expense expense, ExpenseRequest request) {
        if (expense == null || request == null) {
            return;
        }
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description() != null ? request.description().trim() : null);
    }
}
