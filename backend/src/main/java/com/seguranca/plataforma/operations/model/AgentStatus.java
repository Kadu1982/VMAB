package com.seguranca.plataforma.operations.model;

public enum AgentStatus {
    ACTIVE("Ativo"),
    ON_DUTY("Em ServiÃ§o"),
    OFF_DUTY("Fora de ServiÃ§o"),
    BLOCKED("Bloqueado");

    private final String descricao;

    // Construtor do enum
    AgentStatus(String descricao) {
        this.descricao = descricao;
    }

    // MÃ©todo para recuperar o nome delegado
    public String getDescricao() {
        return descricao;
    }
}




