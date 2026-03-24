package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.dto.RequestShiftHandoffRequest;
import com.seguranca.plataforma.operations.dto.RespondShiftHandoffRequest;
import com.seguranca.plataforma.operations.dto.SuperviseShiftRequest;
import com.seguranca.plataforma.operations.dto.UpdateShiftRequest;
import com.seguranca.plataforma.operations.model.Shift;
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
@RequestMapping("/api/shifts")
public class ShiftController {
    // CRUD dos turnos e dos fluxos formais de troca e supervisao de presenca.

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

    @PostMapping("/{id}/handoff-request")
    public Shift requestHandoff(@PathVariable Long id, @Valid @RequestBody RequestShiftHandoffRequest request) {
        // Abre o pedido de troca e deixa a transferencia pendente ate o aceite.
        return operationsService.requestShiftHandoff(id, request);
    }

    @PostMapping("/{id}/handoff-accept")
    public Shift acceptHandoff(@PathVariable Long id, @Valid @RequestBody RespondShiftHandoffRequest request) {
        // O aceite duplo so conclui a troca quando o proximo vigilante confirma assumir.
        return operationsService.acceptShiftHandoff(id, request);
    }

    @PostMapping("/{id}/handoff-reject")
    public Shift rejectHandoff(@PathVariable Long id, @Valid @RequestBody RespondShiftHandoffRequest request) {
        // Permite recusar a troca sem perder a trilha da decisao.
        return operationsService.rejectShiftHandoff(id, request);
    }

    @PostMapping("/{id}/supervision")
    public Shift supervise(@PathVariable Long id, @Valid @RequestBody SuperviseShiftRequest request) {
        // Reune as decisoes de atraso, falta e cobertura em um unico fluxo formal.
        return operationsService.superviseShift(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteShift(id);
    }
}
