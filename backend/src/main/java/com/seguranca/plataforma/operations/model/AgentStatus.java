package com.seguranca.plataforma.operations.model;

public enum AgentStatus {
    ACTIVE("Ativo"),
    ON_DUTY("Em Serviço"),
    OFF_DUTY("Fora de Serviço"),
    BLOCKED("Bloqueado");

    private final String descricao;

    // Construtor do enum
    AgentStatus(String descricao) {
        this.descricao = descricao;
    }

    // Método para recuperar o nome delegado
    public String getDescricao() {
        return descricao;
    }
}


