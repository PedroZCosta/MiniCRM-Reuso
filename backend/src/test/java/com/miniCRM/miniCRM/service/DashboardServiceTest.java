package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.dashboard.DashboardResponse;
import com.miniCRM.miniCRM.dto.funil.TotalEtapa;
import com.miniCRM.miniCRM.dto.tarefa.AlertasTarefas;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClienteService clienteService;
    @Mock
    private OportunidadeService oportunidadeService;
    @Mock
    private TarefaService tarefaService;
    @Mock
    private EscopoCarteira escopoCarteira;

    @InjectMocks
    private DashboardService dashboardService;

    private Map<StatusCliente, Long> clientesPorStatus(long prospect, long ativo, long inativo) {
        Map<StatusCliente, Long> mapa = new EnumMap<>(StatusCliente.class);
        mapa.put(StatusCliente.PROSPECT, prospect);
        mapa.put(StatusCliente.ATIVO, ativo);
        mapa.put(StatusCliente.INATIVO, inativo);
        return mapa;
    }

    private Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa() {
        Map<EtapaOportunidade, TotalEtapa> mapa = new EnumMap<>(EtapaOportunidade.class);
        mapa.put(EtapaOportunidade.PROSPECCAO, new TotalEtapa(3, BigDecimal.valueOf(30000)));
        mapa.put(EtapaOportunidade.CONTATO, new TotalEtapa(2, BigDecimal.valueOf(20000)));
        mapa.put(EtapaOportunidade.PROPOSTA, new TotalEtapa(1, BigDecimal.valueOf(10000)));
        mapa.put(EtapaOportunidade.GANHA, new TotalEtapa(5, BigDecimal.valueOf(50000)));
        mapa.put(EtapaOportunidade.PERDIDA, new TotalEtapa(2, BigDecimal.valueOf(20000)));
        return mapa;
    }

    @Test
    @DisplayName("dashboard do vendedor traz somente os numeros dele")
    void dashboardDoVendedorTrazSomenteOsNumerosDele() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(7)));
        when(clienteService.contarPorStatus(List.of(7))).thenReturn(clientesPorStatus(2, 1, 0));
        when(oportunidadeService.totaisPorEtapa(List.of(7))).thenReturn(totaisPorEtapa());
        when(oportunidadeService.valorEmNegociacao(List.of(7))).thenReturn(BigDecimal.valueOf(60000));
        when(tarefaService.alertas(List.of(7))).thenReturn(new AlertasTarefas(1, 1, 0, 0));

        DashboardResponse resposta = dashboardService.montar();

        assertThat(resposta.totalClientes()).isEqualTo(3);
        assertThat(resposta.valorEmNegociacao()).isEqualByComparingTo(BigDecimal.valueOf(60000));
    }

    @Test
    @DisplayName("dashboard do gerente soma a equipe inteira, diferente do vendedor sozinho")
    void dashboardDoGerenteSomaAEquipeInteira() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(2, 7, 8)));
        when(clienteService.contarPorStatus(List.of(2, 7, 8))).thenReturn(clientesPorStatus(10, 15, 3));
        when(oportunidadeService.totaisPorEtapa(List.of(2, 7, 8))).thenReturn(totaisPorEtapa());
        when(oportunidadeService.valorEmNegociacao(List.of(2, 7, 8))).thenReturn(BigDecimal.valueOf(1485000));
        when(tarefaService.alertas(List.of(2, 7, 8))).thenReturn(new AlertasTarefas(3, 2, 1, 4));

        DashboardResponse resposta = dashboardService.montar();

        assertThat(resposta.totalClientes()).isEqualTo(28);
        assertThat(resposta.valorEmNegociacao()).isEqualByComparingTo(BigDecimal.valueOf(1485000));
        assertThat(resposta.alertasTarefas().em15dias()).isEqualTo(4);
    }

    @Test
    @DisplayName("admin nao aplica filtro de vendedor")
    void adminNaoAplicaFiltroDeVendedor() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.contarPorStatus(isNull())).thenReturn(clientesPorStatus(0, 0, 0));
        when(oportunidadeService.totaisPorEtapa(isNull())).thenReturn(totaisPorEtapa());
        when(oportunidadeService.valorEmNegociacao(isNull())).thenReturn(BigDecimal.ZERO);
        when(tarefaService.alertas(isNull())).thenReturn(new AlertasTarefas(0, 0, 0, 0));

        dashboardService.montar();

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
        verify(oportunidadeService).totaisPorEtapa(captor.capture());
        assertThat(captor.getValue()).isNull();
    }

    @Test
    @DisplayName("oportunidades por etapa ignora ganha e perdida")
    void oportunidadesPorEtapaIgnoraGanhaEPerdida() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.contarPorStatus(isNull())).thenReturn(clientesPorStatus(0, 0, 0));
        when(oportunidadeService.totaisPorEtapa(isNull())).thenReturn(totaisPorEtapa());
        when(oportunidadeService.valorEmNegociacao(isNull())).thenReturn(BigDecimal.ZERO);
        when(tarefaService.alertas(isNull())).thenReturn(new AlertasTarefas(0, 0, 0, 0));

        DashboardResponse resposta = dashboardService.montar();

        assertThat(resposta.oportunidadesPorEtapa()).doesNotContainKeys(
                EtapaOportunidade.GANHA, EtapaOportunidade.PERDIDA);
        assertThat(resposta.oportunidadesAbertas()).isEqualTo(6);
    }
}
