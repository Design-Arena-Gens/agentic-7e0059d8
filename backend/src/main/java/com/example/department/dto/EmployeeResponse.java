package com.example.department.dto;

import com.example.department.domain.EmploymentStatus;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String jobTitle,
        EmploymentStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
