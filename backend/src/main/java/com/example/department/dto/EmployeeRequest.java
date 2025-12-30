package com.example.department.dto;

import com.example.department.domain.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name must be less than 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name must be less than 60 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email address is invalid")
        String email,

        @Size(max = 80, message = "Job title must be less than 80 characters")
        String jobTitle,

        @NotNull(message = "Employment status is required")
        EmploymentStatus status,

        LocalDate startDate,

        LocalDate endDate
) {
}
