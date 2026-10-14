package com.expensetracker.service;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.SummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.mappers.ExpenseMapper;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Spy
    private ExpenseMapper expenseMapper = new ExpenseMapper();

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense sampleExpense;
    private ExpenseRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleExpense = new Expense(
                1L,
                "Groceries",
                new BigDecimal("54.20"),
                Category.FOOD,
                LocalDate.of(2026, 3, 15),
                "Weekly grocery shopping"
        );

        sampleRequest = new ExpenseRequest(
                "Groceries",
                new BigDecimal("54.20"),
                Category.FOOD,
                LocalDate.of(2026, 3, 15),
                "Weekly grocery shopping"
        );
    }

    @Test
    @DisplayName("createExpense should persist and return mapped ExpenseResponse")
    void createExpense_success() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });

        ExpenseResponse response = expenseService.createExpense(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Groceries");
        assertThat(response.amount()).isEqualByComparingTo("54.20");
        assertThat(response.category()).isEqualTo(Category.FOOD);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("getExpenseById should return expense when found")
    void getExpenseById_found() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));

        ExpenseResponse response = expenseService.getExpenseById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("getExpenseById should throw ExpenseNotFoundException when not found")
    void getExpenseById_notFound() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("updateExpense should update existing record")
    void updateExpense_success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseRequest updateRequest = new ExpenseRequest(
                "Supermarket",
                new BigDecimal("75.00"),
                Category.FOOD,
                LocalDate.of(2026, 3, 16),
                "Updated description"
        );

        ExpenseResponse response = expenseService.updateExpense(1L, updateRequest);

        assertThat(response.title()).isEqualTo("Supermarket");
        assertThat(response.amount()).isEqualByComparingTo("75.00");
        verify(expenseRepository).save(sampleExpense);
    }

    @Test
    @DisplayName("updateExpense should throw ExpenseNotFoundException when id is missing")
    void updateExpense_notFound() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(999L, sampleRequest))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deleteExpense should delete existing record")
    void deleteExpense_success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    @DisplayName("deleteExpense should throw ExpenseNotFoundException when id is missing")
    void deleteExpense_notFound() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getExpenses should return filtered list")
    void getExpenses_success() {
        when(expenseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(sampleExpense));

        List<ExpenseResponse> results = expenseService.getExpenses("Groc", Category.FOOD, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("getSummary should return aggregated metrics")
    void getSummary_success() {
        when(expenseRepository.sumAmountBetween(null, null)).thenReturn(new BigDecimal("150.00"));
        when(expenseRepository.countBetween(null, null)).thenReturn(3L);
        when(expenseRepository.sumAmountForCurrentMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("100.00"));

        List<Object[]> breakdownData = Collections.singletonList(new Object[]{Category.FOOD, new BigDecimal("80.00")});
        when(expenseRepository.sumAmountByCategoryBetween(null, null)).thenReturn(breakdownData);

        SummaryResponse summary = expenseService.getSummary(null, null);

        assertThat(summary.totalAmount()).isEqualByComparingTo("150.00");
        assertThat(summary.expenseCount()).isEqualTo(3L);
        assertThat(summary.currentMonthTotal()).isEqualByComparingTo("100.00");
        assertThat(summary.categoryBreakdown()).containsEntry("FOOD", new BigDecimal("80.00"));
        assertThat(summary.categoryBreakdown()).containsEntry("TRANSPORT", BigDecimal.ZERO);
    }
}
