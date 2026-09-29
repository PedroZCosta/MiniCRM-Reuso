package com.miniCRM.miniCRM.processamento;

import com.miniCRM.miniCRM.event.TarefaVencendoEvent;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;
import com.miniCRM.miniCRM.service.NotificacaoReuniaoService;
import com.miniCRM.miniCRM.service.NotificacaoTarefaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoProcessadorTest {

    @Mock
    private NotificacaoTarefaService notificacaoTarefaService;
    @Mock
    private NotificacaoReuniaoService notificacaoReuniaoService;

    @InjectMocks
    private NotificacaoProcessador processador;

    @Test
    @DisplayName("faixa de tarefa vai para o service de tarefa")
    void faixaDeTarefaVaiParaOServiceDeTarefa() {
        TarefaVencendoEvent evento = new TarefaVencendoEvent(10, 1, "Ligar", TipoNotificacao.D1, LocalDate.now());

        processador.aoVencerTarefa(evento);

        verify(notificacaoTarefaService).gerar(evento);
        verify(notificacaoReuniaoService, never()).gerar(evento);
    }

    @Test
    @DisplayName("faixa de reuniao vai para o service de reuniao")
    void faixaDeReuniaoVaiParaOServiceDeReuniao() {
        TarefaVencendoEvent evento = new TarefaVencendoEvent(
                null, 1, "Cliente X", TipoNotificacao.REUNIAO, LocalDate.of(2026, 9, 20));

        processador.aoVencerTarefa(evento);

        verify(notificacaoReuniaoService).gerar(evento);
        verify(notificacaoTarefaService, never()).gerar(evento);
    }
}
