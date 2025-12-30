package com.example.department.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.department.domain.EmploymentStatus;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.EmployeeRequest;
import com.example.department.exception.DuplicateResourceException;
import com.example.department.exception.ResourceNotFoundException;
import com.example.department.repository.DepartmentRepository;
import com.example.department.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(DepartmentService.class)
class DepartmentServiceTests {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private DepartmentRequest engineeringRequest;

    @BeforeEach
    void setUp() {
        engineeringRequest = new DepartmentRequest(
                "Engineering",
                "ENG",
                "Builds products",
                "NY",
                "Ada Lovelace",
                new BigDecimal("1000000"));
    }

    @Test
    void createDepartmentPersistsEntity() {
        var created = departmentService.createDepartment(engineeringRequest);

        assertThat(created.id()).isNotNull();
        assertThat(departmentRepository.count()).isEqualTo(1);
        assertThat(created.code()).isEqualTo("ENG");
    }

    @Test
    void duplicateDepartmentCodeIsRejected() {
        departmentService.createDepartment(engineeringRequest);

        assertThatThrownBy(() -> departmentService.createDepartment(engineeringRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addEmployeeAttachesToDepartment() {
        Long departmentId = departmentService.createDepartment(engineeringRequest).id();

        var employeeRequest = new EmployeeRequest(
                "Grace",
                "Hopper",
                "ghopper@example.com",
                "Principal Engineer",
                EmploymentStatus.ACTIVE,
                LocalDate.of(2010, 1, 10),
                null);

        var response = departmentService.addEmployee(departmentId, employeeRequest);

        assertThat(response.id()).isNotNull();
        assertThat(employeeRepository.count()).isEqualTo(1);
        var department = departmentRepository.findById(departmentId).orElseThrow();
        assertThat(department.getEmployees()).hasSize(1);
    }

    @Test
    void removeDepartmentDeletesEmployees() {
        Long departmentId = departmentService.createDepartment(engineeringRequest).id();
        departmentService.addEmployee(
                departmentId,
                new EmployeeRequest(
                        "Alan",
                        "Turing",
                        "aturing@example.com",
                        "Staff Engineer",
                        EmploymentStatus.ACTIVE,
                        LocalDate.of(2011, 6, 1),
                        null));

        departmentService.deleteDepartment(departmentId);

        assertThat(departmentRepository.count()).isZero();
        assertThat(employeeRepository.count()).isZero();
        assertThatThrownBy(() -> departmentService.getDepartment(departmentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
