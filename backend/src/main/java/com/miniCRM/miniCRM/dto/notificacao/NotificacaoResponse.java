package com.miniCRM.miniCRM.dto.notificacao;

import com.miniCRM.miniCRM.model.Notificacao;
import com.miniCRM.miniCRM.model.enums.TipoNotificacao;

import java.time.LocalDateTime;

/** RF23/RF24: notificacao interna do sistema, sem e-mail. */
public record NotificacaoResponse(
        Integer idNotificacao,
        TipoNotificacao tipo,
        String mensagem,
        boolean lida,
        LocalDateTime geradaEm,
        Integer idTarefa) {

    public static NotificacaoResponse de(Notificacao n) {
        return new NotificacaoResponse(
                n.getIdNotificacao(),
                n.getTipo(),
                n.getMensagem(),
                Boolean.TRUE.equals(n.getLida()),
                n.getGeradaEm(),
                n.getTarefa() != null ? n.getTarefa().getIdTarefa() : null);
    }
}
