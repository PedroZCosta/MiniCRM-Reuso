package com.miniCRM.miniCRM.dto.tarefa;

import com.miniCRM.miniCRM.model.enums.SituacaoTarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** RF28: situacao e vencida saem calculadas, nunca de coluna. */
public record TarefaResponse(
        Integer idTarefa,
        String titulo,
        String descricao,
        LocalDate dataVencimento,
        SituacaoTarefa situacao,
        boolean vencida,
        boolean concluida,
        LocalDateTime concluidaEm,
        String responsavel,
        Integer idCliente,
        String cliente,
        Integer idOportunidade,
        String oportunidade,
        LocalDateTime criadoEm) {
}
