package com.miniCRM.miniCRM.dto.cliente;

import java.time.LocalDateTime;

/** Reunião marcada dentro da janela de varredura do job de notificações (SPEC-04 §3.1). */
public record ReuniaoAgendada(
        Integer idUsuario,
        String cliente,
        LocalDateTime dataInteracao) {
}
