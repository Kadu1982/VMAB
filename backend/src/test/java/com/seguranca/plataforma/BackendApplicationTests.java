package com.seguranca.plataforma;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class BackendApplicationTests {

    @Test
    void applicationClassShouldBeLoadable() {
        // Smoke test leve: garante que a classe de entrada continua existindo sem forcar banco ou Flyway.
        assertDoesNotThrow(() -> Class.forName("com.seguranca.plataforma.BackendApplication"));
    }
}


