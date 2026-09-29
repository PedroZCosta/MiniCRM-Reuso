package com.miniCRM.miniCRM.dto.tarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** RN-02: editar nunca mexe em concluida/concluidaEm. */
public record EditarTarefaRequest(
        @NotBlank @Size(max = 120) String titulo,
        String descricao,
        @NotNull LocalDate dataVencimento) {
}
