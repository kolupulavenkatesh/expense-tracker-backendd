package com.expensetracker.controller;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.SummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<ExpenseResponse> expenses = expenseService.getExpenses(search, category, from, to);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        ExpenseResponse expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        ExpenseResponse updated = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expenses/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        SummaryResponse summary = expenseService.getSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(Category.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(categories);
    }
}
