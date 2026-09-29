package com.miniCRM.miniCRM.job;

import com.miniCRM.miniCRM.service.NotificacaoReuniaoService;
import com.miniCRM.miniCRM.service.NotificacaoTarefaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoJobTest {

    @Mock
    private NotificacaoTarefaService notificacaoTarefaService;
    @Mock
    private NotificacaoReuniaoService notificacaoReuniaoService;

    @InjectMocks
    private NotificacaoJob job;

    @Test
    @DisplayName("execucao varre tarefas e reunioes na mesma data")
    void execucaoVarreTarefasEReunioesNaMesmaData() {
        LocalDate hoje = LocalDate.of(2026, 9, 10);
        when(notificacaoTarefaService.varrer(hoje)).thenReturn(4);
        when(notificacaoReuniaoService.varrer(hoje)).thenReturn(1);

        job.executar(hoje);

        verify(notificacaoTarefaService).varrer(hoje);
        verify(notificacaoReuniaoService).varrer(hoje);
    }

    @Test
    @DisplayName("ao iniciar a aplicacao varre tarefas e reunioes de hoje")
    void aoIniciarAAplicacaoVarreTarefasEReunioesDeHoje() {

        job.aoIniciar();

        verify(notificacaoTarefaService).varrer(LocalDate.now());
        verify(notificacaoReuniaoService).varrer(LocalDate.now());
    }

    @Test
    @DisplayName("execucao periodica varre tarefas e reunioes de hoje")
    void execucaoPeriodicaVarreTarefasEReunioesDeHoje() {

        job.periodicamente();

        verify(notificacaoTarefaService).varrer(LocalDate.now());
        verify(notificacaoReuniaoService).varrer(LocalDate.now());
    }

    @Test
    @DisplayName("rodar o job duas vezes repete a varredura sem acumular estado")
    void rodarOJobDuasVezesRepeteAVarreduraSemAcumularEstado() {
        LocalDate hoje = LocalDate.of(2026, 9, 10);

        job.executar(hoje);
        job.executar(hoje);

        verify(notificacaoTarefaService, times(2)).varrer(hoje);
        verify(notificacaoReuniaoService, times(2)).varrer(hoje);
    }
}
