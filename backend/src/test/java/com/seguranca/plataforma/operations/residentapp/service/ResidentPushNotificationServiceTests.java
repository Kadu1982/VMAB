package com.seguranca.plataforma.operations.residentapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.operations.residentapp.model.ResidentAlert;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertType;
import com.seguranca.plataforma.operations.residentapp.repository.ResidentPushDeviceRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class ResidentPushNotificationServiceTests {

    @Mock
    private ResidentPushDeviceRepository residentPushDeviceRepository;

    private ResidentPushNotificationService residentPushNotificationService;

    @BeforeEach
    void setUp() {
        residentPushNotificationService = new ResidentPushNotificationService(
                residentPushDeviceRepository,
                RestClient.builder(),
                true,
                "https://exp.host/--/api/v2/push/send"
        );
    }

    @Test
    void nãoDeveFalharSemDispositivoAtivo() {
        // Quando não existe token ativo, o fluxo precisa encerrar sem tentar notificar nada.
        when(residentPushDeviceRepository.findByResidentIdAndRevokedAtIsNullOrderByUpdatedAtDesc(1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> residentPushNotificationService.notifyResidentAlertStatusChanged(buildAlert(true, ResidentAlertStatus.ACKNOWLEDGED)));
    }

    @Test
    void deveMontarMensagemDiscretaParaFluxoSilencioso() {
        ResidentPushNotificationService.PushMessage message =
                residentPushNotificationService.buildMessage(buildAlert(true, ResidentAlertStatus.ACKNOWLEDGED));

        assertEquals("Atualizacao de atendimento", message.title());
        assertEquals("Sua solicitacao silenciosa foi recebida pela central.", message.body());
    }

    private ResidentAlert buildAlert(boolean silent, ResidentAlertStatus status) {
        return new ResidentAlert(
                1L,
                "Morador VMAB",
                "(11) 99999-1111",
                "Rua A, 100",
                silent ? ResidentAlertType.COACAO : ResidentAlertType.PANICO,
                status,
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5),
                OffsetDateTime.now(ZoneOffset.UTC),
                null,
                null,
                "Teste",
                silent,
                null
        );
    }
}


