package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/shifts")
public class ShiftController {

    private final OperationsService operationsService;

    public ShiftController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<Shift> list() {
        return operationsService.listShifts();
    }

    @PostMapping
    public Shift create(@Valid @RequestBody CreateShiftRequest request) {
        return operationsService.addShift(request);
    }
}
