package com.example.department.service;

import com.example.department.domain.Department;
import com.example.department.domain.Employee;
import com.example.department.dto.DepartmentDetailResponse;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.DepartmentSummaryResponse;
import com.example.department.dto.EmployeeRequest;
import com.example.department.dto.EmployeeResponse;
import com.example.department.exception.BusinessValidationException;
import com.example.department.exception.DuplicateResourceException;
import com.example.department.exception.ResourceNotFoundException;
import com.example.department.repository.DepartmentRepository;
import com.example.department.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper mapper = new DepartmentMapper();

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public DepartmentDetailResponse createDepartment(DepartmentRequest request) {
        String normalizedCode = normalizeCode(request.code());
        if (departmentRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateResourceException("Department code already exists: " + normalizedCode);
        }
        Department department = new Department();
        mapper.updateEntity(department, request);
        department = departmentRepository.save(department);
        return mapper.toDetail(department);
    }

    @Transactional
    public DepartmentDetailResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = getDepartmentEntity(id);
        String normalizedCode = normalizeCode(request.code());
        departmentRepository.findByCodeIgnoreCase(normalizedCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Department code already exists: " + normalizedCode);
                });
        mapper.updateEntity(department, request);
        return mapper.toDetail(department);
    }

    @Transactional
    public EmployeeResponse addEmployee(Long departmentId, EmployeeRequest request) {
        Department department = getDepartmentEntity(departmentId);
        employeeRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Employee email already exists: " + existing.getEmail());
                });
        if (request.endDate() != null && request.startDate() != null
                && request.endDate().isBefore(request.startDate())) {
            throw new BusinessValidationException("Employee end date cannot be before start date");
        }
        Employee employee = mapper.toEmployee(department, request);
        employeeRepository.save(employee);
        return mapper.toEmployeeResponse(employee);
    }

    @Transactional
    public void removeEmployee(Long departmentId, Long employeeId) {
        Department department = getDepartmentEntity(departmentId);
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> e.getDepartment() != null && e.getDepartment().getId().equals(departmentId))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee not found in department: " + employeeId));
        department.removeEmployee(employee);
        employeeRepository.delete(employee);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartmentEntity(id);
        departmentRepository.delete(department);
    }

    @Transactional
    public DepartmentDetailResponse getDepartment(Long id) {
        Department department = getDepartmentEntity(id);
        department.getEmployees().size(); // ensure loaded
        return mapper.toDetail(department);
    }

    @Transactional
    public List<DepartmentSummaryResponse> listDepartments(String query) {
        String normalized = StringUtils.hasText(query) ? query.trim() : "";
        List<Department> departments;
        if (normalized.isEmpty()) {
            departments = departmentRepository.findAll();
        } else {
            departments = departmentRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                    normalized, normalized);
        }
        return departments.stream()
                .map(mapper::toSummary)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }

    private Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessValidationException("Department code is required");
        }
        return code.trim().toUpperCase();
    }
}
