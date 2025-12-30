package com.example.department.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record DepartmentDetailResponse(
        Long id,
        String name,
        String code,
        String description,
        String location,
        String head,
        BigDecimal annualBudget,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<EmployeeResponse> employees
) {
}
