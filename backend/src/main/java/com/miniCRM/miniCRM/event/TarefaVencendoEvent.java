package com.miniCRM.miniCRM.event;

import com.miniCRM.miniCRM.model.enums.TipoNotificacao;

import java.time.LocalDate;

/**
 * Contrato imutável da SPEC-00 §6. Publicado pelos services de notificação (varredura do job) e
 * consumido pelo NotificacaoProcessador. Não alterar o payload sem combinar com o grupo.
 *
 * <p>Convenção para a faixa REUNIAO, que não tem tarefa: {@code idTarefa = null},
 * {@code tituloTarefa = nome do cliente} e {@code dataVencimento = dia da reunião}.
 */
public record TarefaVencendoEvent(
        Integer idTarefa,
        Integer idUsuario,
        String tituloTarefa,
        TipoNotificacao faixa,
        LocalDate dataVencimento) {
}
