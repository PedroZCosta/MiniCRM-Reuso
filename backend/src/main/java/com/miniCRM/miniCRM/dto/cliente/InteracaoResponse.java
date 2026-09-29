package com.miniCRM.miniCRM.dto.cliente;

import com.miniCRM.miniCRM.model.enums.TipoInteracao;

import java.time.LocalDateTime;

public record InteracaoResponse(
        Integer idInteracao,
        TipoInteracao tipo,
        LocalDateTime dataInteracao,
        String observacoes,
        String autor,
        LocalDateTime criadoEm) {
}
