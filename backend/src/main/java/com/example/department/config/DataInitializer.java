package com.example.department.config;

import com.example.department.domain.EmploymentStatus;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.EmployeeRequest;
import com.example.department.service.DepartmentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(DepartmentService departmentService) {
        return args -> {
            if (!departmentService.listDepartments(null).isEmpty()) {
                return;
            }

            DepartmentRequest engineering = new DepartmentRequest(
                    "Engineering",
                    "ENG",
                    "Responsible for product development and innovation.",
                    "New York",
                    "Ada Lovelace",
                    new BigDecimal("2500000"));

            DepartmentRequest hr = new DepartmentRequest(
                    "Human Resources",
                    "HR",
                    "Manages recruitment, onboarding, and employee wellbeing.",
                    "Remote",
                    "Mary Parker",
                    new BigDecimal("750000"));

            DepartmentRequest finance = new DepartmentRequest(
                    "Finance",
                    "FIN",
                    "Oversees budgeting, forecasting, and compliance.",
                    "Chicago",
                    "Alan Turing",
                    new BigDecimal("1500000"));

            Long engineeringId = departmentService.createDepartment(engineering).id();
            Long hrId = departmentService.createDepartment(hr).id();
            Long financeId = departmentService.createDepartment(finance).id();

            departmentService.addEmployee(engineeringId, new EmployeeRequest(
                    "Grace",
                    "Hopper",
                    "grace.hopper@example.com",
                    "Principal Engineer",
                    EmploymentStatus.ACTIVE,
                    LocalDate.of(2015, 4, 23),
                    null));

            departmentService.addEmployee(engineeringId, new EmployeeRequest(
                    "Linus",
                    "Torvalds",
                    "linus.torvalds@example.com",
                    "Staff Engineer",
                    EmploymentStatus.ON_LEAVE,
                    LocalDate.of(2018, 11, 4),
                    null));

            departmentService.addEmployee(hrId, new EmployeeRequest(
                    "Patricia",
                    "Diaz",
                    "patricia.diaz@example.com",
                    "HR Specialist",
                    EmploymentStatus.ACTIVE,
                    LocalDate.of(2020, 1, 15),
                    null));

            departmentService.addEmployee(financeId, new EmployeeRequest(
                    "Noah",
                    "Kim",
                    "noah.kim@example.com",
                    "Financial Analyst",
                    EmploymentStatus.ACTIVE,
                    LocalDate.of(2019, 7, 1),
                    null));
        };
    }
}
