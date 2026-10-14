package com.expensetracker.controller;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.SummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Test
    @DisplayName("POST /api/expenses with valid body returns 201 Created and ExpenseResponse")
    void createExpense_valid_returns201() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                "Lunch with Team",
                new BigDecimal("25.50"),
                Category.FOOD,
                LocalDate.of(2026, 3, 20),
                "Team lunch"
        );

        ExpenseResponse response = new ExpenseResponse(
                1L,
                "Lunch with Team",
                new BigDecimal("25.50"),
                Category.FOOD,
                LocalDate.of(2026, 3, 20),
                "Team lunch",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Lunch with Team"))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    @DisplayName("POST /api/expenses with invalid body (amount -1, empty title) returns 400 with fieldErrors")
    void createExpense_invalidBody_returns400() throws Exception {
        String invalidJson = """
                {
                    "title": "",
                    "amount": -1,
                    "category": "FOOD",
                    "expenseDate": "2026-03-20"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    @DisplayName("GET /api/expenses/{id} when missing returns 404 Not Found")
    void getExpenseById_missing_returns404() throws Exception {
        when(expenseService.getExpenseById(999999L)).thenThrow(new ExpenseNotFoundException(999999L));

        mockMvc.perform(get("/api/expenses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    @Test
    @DisplayName("GET /api/expenses returns 200 with list of expenses")
    void getExpenses_returns200() throws Exception {
        ExpenseResponse item = new ExpenseResponse(
                1L,
                "Bus ticket",
                new BigDecimal("3.50"),
                Category.TRANSPORT,
                LocalDate.of(2026, 3, 20),
                "Commute",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(expenseService.getExpenses(any(), any(), any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/expenses?category=TRANSPORT&search=Bus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Bus ticket"));
    }

    @Test
    @DisplayName("PUT /api/expenses/{id} returns 200 on update")
    void updateExpense_returns200() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                "Updated title",
                new BigDecimal("40.00"),
                Category.BILLS,
                LocalDate.of(2026, 3, 20),
                "Updated"
        );

        ExpenseResponse response = new ExpenseResponse(
                1L,
                "Updated title",
                new BigDecimal("40.00"),
                Category.BILLS,
                LocalDate.of(2026, 3, 20),
                "Updated",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.amount").value(40.00));
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} returns 204 No Content")
    void deleteExpense_returns204() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} when missing returns 404")
    void deleteExpense_missing_returns404() throws Exception {
        doThrow(new ExpenseNotFoundException(999L)).when(expenseService).deleteExpense(999L);

        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/expenses/summary returns 200 with summary metrics")
    void getSummary_returns200() throws Exception {
        SummaryResponse summary = new SummaryResponse(
                new BigDecimal("200.00"),
                5,
                new BigDecimal("150.00"),
                Map.of("FOOD", new BigDecimal("100.00"), "TRANSPORT", new BigDecimal("50.00"))
        );

        when(expenseService.getSummary(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.expenseCount").value(5))
                .andExpect(jsonPath("$.categoryBreakdown.FOOD").value(100.00));
    }

    @Test
    @DisplayName("GET /api/categories returns 200 with list of category strings")
    void getCategories_returns200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("FOOD")))
                .andExpect(jsonPath("$", hasItem("TRANSPORT")))
                .andExpect(jsonPath("$", hasItem("SHOPPING")));
    }
}
