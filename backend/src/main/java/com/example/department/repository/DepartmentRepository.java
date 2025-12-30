package com.example.department.repository;

import com.example.department.domain.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Department> findByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = "employees")
    List<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}
