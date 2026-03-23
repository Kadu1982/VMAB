package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateResidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateResidentRequest;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/residents")
public class ResidentController {
    // CRUD do cadastro de moradores protegidos.

    private final OperationsService operationsService;

    public ResidentController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<Resident> list() {
        return operationsService.listResidents();
    }

    @PostMapping
    public Resident create(@Valid @RequestBody CreateResidentRequest request) {
        return operationsService.addResident(request);
    }

    @PutMapping("/{id}")
    public Resident update(@PathVariable Long id, @Valid @RequestBody UpdateResidentRequest request) {
        return operationsService.updateResident(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteResident(id);
    }
}
