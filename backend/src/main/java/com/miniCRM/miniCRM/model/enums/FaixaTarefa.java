package com.miniCRM.miniCRM.model.enums;

import java.time.LocalDate;

/**
 * Faixas da tela inicial (RF13). Concentra a aritmética de datas das tarefas: o filtro da listagem,
 * os cards do dashboard e o job de notificações leem daqui.
 *
 * <p>São duas noções diferentes: {@link #classificarParaNotificacao} usa o dia exato (uma tarefa que
 * vence amanhã nunca recebe "vence em 15 dias"), e {@link #inicio}/{@link #fim} devolvem a janela
 * usada pelo filtro {@code ?faixa=} e pelo card do dashboard ("em 15 dias" = os próximos 15 dias).
 */
public enum FaixaTarefa {
    D15,
    D1,
    HOJE,
    VENCIDA;

    public static final int DIAS_ANTECEDENCIA = 15;

    /** Faixa de uma tarefa nao concluida pelo dia exato, ou null quando nao cai em faixa nenhuma. */
    public static FaixaTarefa classificarParaNotificacao(LocalDate vencimento, LocalDate hoje) {
        if (vencimento.isBefore(hoje)) return VENCIDA;
        if (vencimento.isEqual(hoje)) return HOJE;
        if (vencimento.isEqual(hoje.plusDays(1))) return D1;
        if (vencimento.isEqual(hoje.plusDays(DIAS_ANTECEDENCIA))) return D15;
        return null;
    }

    /** Menor data de vencimento da janela; null = sem limite inferior. */
    public LocalDate inicio(LocalDate hoje) {
        return switch (this) {
            case VENCIDA -> null;
            case HOJE -> hoje;
            case D1 -> hoje.plusDays(1);
            case D15 -> hoje.plusDays(2);
        };
    }

    /** Maior data de vencimento da janela. */
    public LocalDate fim(LocalDate hoje) {
        return switch (this) {
            case VENCIDA -> hoje.minusDays(1);
            case HOJE -> hoje;
            case D1 -> hoje.plusDays(1);
            case D15 -> hoje.plusDays(DIAS_ANTECEDENCIA);
        };
    }

    /** Ponte para o tipo de notificação correspondente. */
    public TipoNotificacao tipoNotificacao() {
        return switch (this) {
            case D15 -> TipoNotificacao.D15;
            case D1 -> TipoNotificacao.D1;
            case HOJE -> TipoNotificacao.HOJE;
            case VENCIDA -> TipoNotificacao.VENCIDA;
        };
    }
}
