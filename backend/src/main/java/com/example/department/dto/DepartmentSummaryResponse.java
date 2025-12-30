package com.example.department.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DepartmentSummaryResponse(
        Long id,
        String name,
        String code,
        String head,
        String location,
        BigDecimal annualBudget,
        int employeeCount,
        OffsetDateTime updatedAt
) {
}
