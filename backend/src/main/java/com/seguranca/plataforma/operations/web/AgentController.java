package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateAgentRequest;
import com.seguranca.plataforma.operations.dto.UpdateAgentRequest;
import com.seguranca.plataforma.operations.model.Agent;
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
@RequestMapping("/api/agents")
public class AgentController {
    // CRUD da equipe de ronda.

    private final OperationsService operationsService;

    public AgentController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<Agent> list() {
        return operationsService.listAgents();
    }

    @PostMapping
    public Agent create(@Valid @RequestBody CreateAgentRequest request) {
        return operationsService.addAgent(request);
    }

    @PutMapping("/{id}")
    public Agent update(@PathVariable Long id, @Valid @RequestBody UpdateAgentRequest request) {
        return operationsService.updateAgent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteAgent(id);
    }
}


