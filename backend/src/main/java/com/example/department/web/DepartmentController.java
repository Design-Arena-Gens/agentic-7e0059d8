package com.example.department.web;

import com.example.department.dto.DepartmentDetailResponse;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.DepartmentSummaryResponse;
import com.example.department.dto.EmployeeRequest;
import com.example.department.dto.EmployeeResponse;
import com.example.department.service.DepartmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentSummaryResponse> list(@RequestParam(name = "q", required = false) String query) {
        return departmentService.listDepartments(query);
    }

    @GetMapping("/{id}")
    public DepartmentDetailResponse get(@PathVariable Long id) {
        return departmentService.getDepartment(id);
    }

    @PostMapping
    public ResponseEntity<DepartmentDetailResponse> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentDetailResponse created = departmentService.createDepartment(request);
        return ResponseEntity.created(URI.create("/api/departments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public DepartmentDetailResponse update(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentService.updateDepartment(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<EmployeeResponse> addEmployee(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = departmentService.addEmployee(id, request);
        return ResponseEntity.created(URI.create("/api/departments/" + id + "/employees/" + employee.id()))
                .body(employee);
    }

    @DeleteMapping("/{departmentId}/employees/{employeeId}")
    public ResponseEntity<Void> removeEmployee(@PathVariable Long departmentId, @PathVariable Long employeeId) {
        departmentService.removeEmployee(departmentId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
