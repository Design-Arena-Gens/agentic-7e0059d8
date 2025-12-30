package com.example.department.service;

import com.example.department.domain.Department;
import com.example.department.domain.Employee;
import com.example.department.dto.DepartmentDetailResponse;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.DepartmentSummaryResponse;
import com.example.department.dto.EmployeeRequest;
import com.example.department.dto.EmployeeResponse;
import java.util.Comparator;
import java.util.List;

public class DepartmentMapper {

    public DepartmentSummaryResponse toSummary(Department department) {
        return new DepartmentSummaryResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getHead(),
                department.getLocation(),
                department.getAnnualBudget(),
                department.getEmployees().size(),
                department.getUpdatedAt());
    }

    public DepartmentDetailResponse toDetail(Department department) {
        List<EmployeeResponse> employees = department.getEmployees().stream()
                .sorted(Comparator.comparing(Employee::getLastName).thenComparing(Employee::getFirstName))
                .map(this::toEmployeeResponse)
                .toList();
        return new DepartmentDetailResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getLocation(),
                department.getHead(),
                department.getAnnualBudget(),
                department.getCreatedAt(),
                department.getUpdatedAt(),
                employees);
    }

    public void updateEntity(Department department, DepartmentRequest request) {
        department.setName(request.name().trim());
        department.setCode(request.code().trim().toUpperCase());
        department.setDescription(trimToNull(request.description()));
        department.setLocation(trimToNull(request.location()));
        department.setHead(trimToNull(request.head()));
        department.setAnnualBudget(request.annualBudget());
    }

    public Employee toEmployee(Department department, EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setJobTitle(trimToNull(request.jobTitle()));
        employee.setStatus(request.status());
        employee.setStartDate(request.startDate());
        employee.setEndDate(request.endDate());
        department.addEmployee(employee);
        return employee;
    }

    public EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getStatus(),
                employee.getStartDate(),
                employee.getEndDate());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
