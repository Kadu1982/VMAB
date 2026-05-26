package com.seguranca.plataforma.hr.web;

import com.seguranca.plataforma.hr.dto.CreateHrEmployeeRequest;
import com.seguranca.plataforma.hr.dto.HrAttendanceResponse;
import com.seguranca.plataforma.hr.dto.HrEmployeeResponse;
import com.seguranca.plataforma.hr.dto.HrSummaryResponse;
import com.seguranca.plataforma.hr.dto.RecordHrAttendanceRequest;
import com.seguranca.plataforma.hr.dto.UpdateHrEmployeeRequest;
import com.seguranca.plataforma.hr.service.HrService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr")
public class HrController {
    private final HrService hrService;

    public HrController(HrService hrService) {
        this.hrService = hrService;
    }

    @GetMapping("/summary")
    public HrSummaryResponse summary() {
        return hrService.summary();
    }

    @GetMapping("/employees")
    public List<HrEmployeeResponse> listEmployees() {
        return hrService.listEmployees();
    }

    @PostMapping("/employees")
    public HrEmployeeResponse createEmployee(@Valid @RequestBody CreateHrEmployeeRequest request) {
        return hrService.createEmployee(request);
    }

    @PutMapping("/employees/{id}")
    public HrEmployeeResponse updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateHrEmployeeRequest request) {
        return hrService.updateEmployee(id, request);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminateEmployee(@PathVariable Long id) {
        hrService.terminateEmployee(id);
    }

    @GetMapping("/attendance")
    public List<HrAttendanceResponse> listAttendance() {
        return hrService.listAttendance();
    }

    @PostMapping("/employees/{id}/attendance")
    public HrAttendanceResponse recordAttendance(@PathVariable Long id, @Valid @RequestBody RecordHrAttendanceRequest request) {
        return hrService.recordAttendance(id, request);
    }
}
