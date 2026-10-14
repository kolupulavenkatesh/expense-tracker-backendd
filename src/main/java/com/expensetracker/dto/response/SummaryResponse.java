package com.expensetracker.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(
        BigDecimal totalAmount,
        long expenseCount,
        BigDecimal currentMonthTotal,
        Map<String, BigDecimal> categoryBreakdown
) {
}
