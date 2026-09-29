package com.miniCRM.miniCRM.dto.dashboard;

import com.miniCRM.miniCRM.dto.tarefa.AlertasTarefas;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.model.enums.StatusCliente;

import java.math.BigDecimal;
import java.util.Map;

/** RF11/RF15: um unico DTO com todos os indicadores, ja recortados pelo EscopoCarteira. */
public record DashboardResponse(
        long totalClientes,
        Map<StatusCliente, Long> clientesPorStatus,
        long oportunidadesAbertas,
        Map<EtapaOportunidade, Long> oportunidadesPorEtapa,
        BigDecimal valorEmNegociacao,
        AlertasTarefas alertasTarefas) {
}
