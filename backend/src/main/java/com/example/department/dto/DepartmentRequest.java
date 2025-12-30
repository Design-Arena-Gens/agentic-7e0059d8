package com.example.department.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 120, message = "Department name must be less than 120 characters")
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 40, message = "Department code must be less than 40 characters")
        String code,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @Size(max = 80, message = "Location must be less than 80 characters")
        String location,

        @Size(max = 80, message = "Department head must be less than 80 characters")
        String head,

        @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
        BigDecimal annualBudget
) {
}
