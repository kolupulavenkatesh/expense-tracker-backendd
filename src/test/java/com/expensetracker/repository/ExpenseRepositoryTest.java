package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        Expense e1 = new Expense(
                null,
                "Supermarket Groceries",
                new BigDecimal("85.50"),
                Category.FOOD,
                LocalDate.of(2026, 3, 5),
                "Weekly fruit and vegetables"
        );

        Expense e2 = new Expense(
                null,
                "Train Ticket",
                new BigDecimal("15.00"),
                Category.TRANSPORT,
                LocalDate.of(2026, 3, 12),
                "Commute to office"
        );

        Expense e3 = new Expense(
                null,
                "Electric Utility",
                new BigDecimal("120.00"),
                Category.BILLS,
                LocalDate.of(2026, 3, 25),
                "Monthly power bill"
        );

        Expense e4 = new Expense(
                null,
                "Cinema Ticket",
                new BigDecimal("30.00"),
                Category.ENTERTAINMENT,
                LocalDate.of(2026, 4, 10),
                "Weekend movie"
        );

        expenseRepository.saveAll(List.of(e1, e2, e3, e4));
    }

    @Test
    @DisplayName("Filter by category should return only matching category records")
    void filterByCategory() {
        Specification<Expense> spec = ExpenseSpecifications.withFilters(null, Category.FOOD, null, null);
        List<Expense> results = expenseRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Supermarket Groceries");
        assertThat(results.get(0).getCategory()).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Filter by date range should return records within range")
    void filterByDateRange() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);

        Specification<Expense> spec = ExpenseSpecifications.withFilters(null, null, from, to);
        List<Expense> results = expenseRepository.findAll(spec);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(Expense::getTitle)
                .containsExactlyInAnyOrder("Supermarket Groceries", "Train Ticket", "Electric Utility");
    }

    @Test
    @DisplayName("Filter by search term should search in title and description")
    void filterBySearchTerm() {
        // Search matching title
        Specification<Expense> titleSpec = ExpenseSpecifications.withFilters("supermarket", null, null, null);
        List<Expense> titleResults = expenseRepository.findAll(titleSpec);
        assertThat(titleResults).hasSize(1);
        assertThat(titleResults.get(0).getTitle()).isEqualTo("Supermarket Groceries");

        // Search matching description
        Specification<Expense> descSpec = ExpenseSpecifications.withFilters("commute", null, null, null);
        List<Expense> descResults = expenseRepository.findAll(descSpec);
        assertThat(descResults).hasSize(1);
        assertThat(descResults.get(0).getTitle()).isEqualTo("Train Ticket");
    }

    @Test
    @DisplayName("Aggregated summary methods should return correct totals")
    void summaryCalculations() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);

        BigDecimal marchTotal = expenseRepository.sumAmountBetween(from, to);
        long marchCount = expenseRepository.countBetween(from, to);

        assertThat(marchTotal).isEqualByComparingTo("220.50");
        assertThat(marchCount).isEqualTo(3);

        List<Object[]> categorySums = expenseRepository.sumAmountByCategoryBetween(from, to);
        assertThat(categorySums).hasSize(3);
    }
}
