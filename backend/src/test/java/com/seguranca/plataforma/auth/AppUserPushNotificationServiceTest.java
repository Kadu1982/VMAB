package com.seguranca.plataforma.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.IncidentType;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class AppUserPushNotificationServiceTest {

    @Test
    void buildIncidentMessageDeveRefletirStatusOperacional() {
        AppUserPushNotificationService service = new AppUserPushNotificationService(
                null,
                null,
                RestClient.builder(),
                false,
                "https://exp.host/--/api/v2/push/send"
        );
        Incident incident = new Incident(
                IncidentType.PANIC,
                IncidentPriority.HIGH,
                IncidentStatus.ON_SITE,
                "Morador VMAB",
                "Rua A, 100",
                OffsetDateTime.now(ZoneOffset.UTC),
                1L,
                "Carlos",
                2L,
                "ABC1D23",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC),
                null,
                "Despacho ok",
                "Equipe no local",
                null
        );

        var message = service.buildIncidentMessage(incident);

        assertEquals("Equipe no local", message.title());
        assertTrue(message.body().contains("Morador VMAB"));
    }
}


