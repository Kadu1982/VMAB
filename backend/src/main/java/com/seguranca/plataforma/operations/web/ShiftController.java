package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.dto.UpdateShiftRequest;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    // CRUD dos turnos e da vinculacao agente/viatura.

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

    @PutMapping("/{id}")
    public Shift update(@PathVariable Long id, @Valid @RequestBody UpdateShiftRequest request) {
        return operationsService.updateShift(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteShift(id);
    }
}
