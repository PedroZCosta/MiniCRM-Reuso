package com.miniCRM.miniCRM.dto.tarefa;

/** Cards de alerta da tela inicial (RF13), consumidos pelo dashboard (§4.1). */
public record AlertasTarefas(
        long vencidas,
        long hoje,
        long amanha,
        long em15dias) {
}
