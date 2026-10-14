package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecifications {

    private ExpenseSpecifications() {
        // Private constructor for utility class
    }

    public static Specification<Expense> withFilters(String search, Category category, LocalDate from, LocalDate to) {
        Specification<Expense> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(hasSearch(search.trim()));
        }

        if (category != null) {
            spec = spec.and(hasCategory(category));
        }

        if (from != null) {
            spec = spec.and(afterOrEqualDate(from));
        }

        if (to != null) {
            spec = spec.and(beforeOrEqualDate(to));
        }

        return spec;
    }

    public static Specification<Expense> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Expense> hasCategory(Category category) {
        return (root, query, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Expense> afterOrEqualDate(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
        };
    }

    public static Specification<Expense> beforeOrEqualDate(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("expenseDate"), to);
        };
    }
}
