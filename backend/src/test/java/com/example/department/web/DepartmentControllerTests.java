package com.example.department.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.department.domain.EmploymentStatus;
import com.example.department.dto.DepartmentRequest;
import com.example.department.dto.EmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private DepartmentRequest departmentRequest;

    @BeforeEach
    void init() {
        departmentRequest = new DepartmentRequest(
                "Research",
                "RES",
                "Explores new technologies",
                "San Francisco",
                "Elena Gilbert",
                new BigDecimal("500000"));
    }

    @Test
    void listDepartmentsReturnsSeedData() throws Exception {
        String payload = mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(payload).contains("Engineering");
    }

    @Test
    void createDepartmentThenAddEmployee() throws Exception {
        String departmentResponse = mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long departmentId = objectMapper.readTree(departmentResponse).get("id").asLong();

        EmployeeRequest employeeRequest = new EmployeeRequest(
                "Nora",
                "Roberts",
                "nora.roberts@example.com",
                "Research Analyst",
                EmploymentStatus.ACTIVE,
                LocalDate.of(2022, 5, 1),
                null);

        String employeeResponse = mockMvc.perform(post("/api/departments/" + departmentId + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long employeeId = objectMapper.readTree(employeeResponse).get("id").asLong();

        String details = mockMvc.perform(get("/api/departments/" + departmentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(details).contains("nora.roberts@example.com");

        mockMvc.perform(delete("/api/departments/" + departmentId + "/employees/" + employeeId))
                .andExpect(status().isNoContent());

        String afterDelete = mockMvc.perform(get("/api/departments/" + departmentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(afterDelete).doesNotContain("nora.roberts@example.com");
    }
}
