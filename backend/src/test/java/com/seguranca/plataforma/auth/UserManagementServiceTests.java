package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.operations.repository.AgentRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTests {

    @Mock private AppUserRepository appUserRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementService(appUserRepository, agentRepository, passwordEncoder);
    }

    @Test
    void deveListarUsuarioSemAgenteVinculadoSemEstourarNullPointer() {
        AppUser admin = new AppUser("admin", "hash", AppUserRole.ADMIN, true, OffsetDateTime.now(ZoneOffset.UTC));
        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.update("admin", AppUserRole.ADMIN, true, null);

        when(appUserRepository.findAll()).thenReturn(List.of(admin));

        List<AppUserResponse> users = userManagementService.listUsers();

        assertEquals(1, users.size());
        assertEquals("admin", users.getFirst().username());
        assertNull(users.getFirst().linkedAgentId());
        assertNull(users.getFirst().linkedAgentName());
    }
}
