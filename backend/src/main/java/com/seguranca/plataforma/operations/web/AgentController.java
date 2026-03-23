package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateAgentRequest;
import com.seguranca.plataforma.operations.model.Agent;
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
@RequestMapping("/api/agents")
public class AgentController {

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
}
