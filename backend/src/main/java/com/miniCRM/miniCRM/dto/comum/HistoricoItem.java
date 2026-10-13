package com.miniCRM.miniCRM.dto.comum;

import java.time.LocalDateTime;

/** Linha da timeline unificada do cliente (RF06/RF16). */
public record HistoricoItem(
        TipoHistoricoItem tipo,
        LocalDateTime data,
        String titulo,
        String descricao,
        String autor) {
}
