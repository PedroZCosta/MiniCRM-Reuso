package com.miniCRM.miniCRM.dto.cliente;

import com.miniCRM.miniCRM.model.enums.TipoInteracao;

import java.time.LocalDateTime;

public record CriarInteracaoRequest(
        TipoInteracao tipo,
        LocalDateTime dataInteracao,
        String observacoes) {
}
