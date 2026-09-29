package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteRelatorioLinha;
import com.miniCRM.miniCRM.dto.relatorio.ArquivoExportado;
import com.miniCRM.miniCRM.exception.RequisicaoInvalidaException;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private ClienteService clienteService;
    @Mock
    private OportunidadeService oportunidadeService;
    @Mock
    private EscopoCarteira escopoCarteira;

    private RelatorioService relatorioService;

    @BeforeEach
    void montarService() {
        // CsvExportadorService real: é lógica pura, não precisa de mock.
        relatorioService = new RelatorioService(List.of(new CsvExportadorService()),
                clienteService, oportunidadeService, escopoCarteira);
    }

    private String texto(ArquivoExportado arquivo) {
        String bruto = new String(arquivo.conteudo(), StandardCharsets.UTF_8);
        return bruto.startsWith("﻿") ? bruto.substring(1) : bruto;
    }

    @Test
    @DisplayName("csv traz todas as linhas do filtro mesmo com quarenta e cinco registros")
    void csvTrazTodasAsLinhasDoFiltroMesmoComQuarentaECinco() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        List<ClienteRelatorioLinha> linhas = new ArrayList<>();
        for (int i = 1; i <= 45; i++) {
            linhas.add(new ClienteRelatorioLinha("Cliente " + i, "c" + i + "@email.com",
                    "119999" + i, "Empresa " + i, StatusCliente.ATIVO, "Vendedor"));
        }
        when(clienteService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(linhas);

        ArquivoExportado arquivo = relatorioService.clientes("csv", null, null, null);
        String[] linhasDoArquivo = texto(arquivo).split("\r\n");

        assertThat(linhasDoArquivo).hasSize(46);   // header + 45 (RN-06: nunca pagina)
    }

    @Test
    @DisplayName("csv comeca com bom e colunas exatas do rf12 para clientes")
    void csvComecaComBomEColunasExatasDoRf12() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of());

        ArquivoExportado arquivo = relatorioService.clientes("csv", null, null, null);

        assertThat(arquivo.conteudo()[0] & 0xFF).isEqualTo(0xEF);
        assertThat(texto(arquivo)).startsWith("Nome;E-mail;Telefone;Empresa;Status;Vendedor responsável");
    }

    @Test
    @DisplayName("colunas do relatorio de oportunidades seguem o rf12")
    void colunasDoRelatorioDeOportunidadesSeguemORf12() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of());

        ArquivoExportado arquivo = relatorioService.oportunidades("csv", null, null, null);

        assertThat(texto(arquivo)).startsWith("Cliente;Valor estimado;Etapa;Data prevista;Vendedor responsável");
    }

    @Test
    @DisplayName("periodo informado e repassado para a consulta")
    void periodoInformadoEhRepassadoParaAConsulta() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of());

        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 9, 10);
        relatorioService.clientes("csv", StatusCliente.ATIVO, inicio, fim);

        verify(clienteService).linhasParaRelatorio(
                eq(StatusCliente.ATIVO), eq(inicio), eq(fim), isNull());
    }

    @Test
    @DisplayName("escopo do gerente e aplicado antes da exportacao")
    void escopoDoGerenteEhAplicadoAntesDaExportacao() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(2, 7)));
        when(clienteService.linhasParaRelatorio(any(), any(), any(), eq(List.of(2, 7))))
                .thenReturn(List.of());

        relatorioService.clientes("csv", null, null, null);

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
        verify(clienteService).linhasParaRelatorio(any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).containsExactly(2, 7);
    }

    @Test
    @DisplayName("formato desconhecido e requisicao invalida")
    void formatoDesconhecidoEhRequisicaoInvalida() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> relatorioService.clientes("xml", null, null, null))
                .isInstanceOf(RequisicaoInvalidaException.class);
    }

    @Test
    @DisplayName("nome do arquivo termina com a extensao do formato")
    void nomeDoArquivoTerminaComAExtensaoDoFormato() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(clienteService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of());

        ArquivoExportado arquivo = relatorioService.clientes("csv", null, null, null);

        assertThat(arquivo.nomeArquivo()).endsWith(".csv");
    }

    @Test
    @DisplayName("valor e data saem no padrao brasileiro nas oportunidades")
    void valorEDataSaemNoPadraoBrasileiro() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.linhasParaRelatorio(any(), any(), any(), any())).thenReturn(List.of(
                new com.miniCRM.miniCRM.dto.funil.OportunidadeRelatorioLinha(
                        "Construtora Horizonte", new java.math.BigDecimal("1485000.00"),
                        com.miniCRM.miniCRM.model.enums.EtapaOportunidade.PROPOSTA,
                        LocalDate.of(2026, 9, 20), "Juliana Torres")));

        ArquivoExportado arquivo = relatorioService.oportunidades("csv", null, null, null);
        String texto = texto(arquivo);

        assertThat(texto).contains("1.485.000,00");
        assertThat(texto).contains("20/09/2026");
    }
}
