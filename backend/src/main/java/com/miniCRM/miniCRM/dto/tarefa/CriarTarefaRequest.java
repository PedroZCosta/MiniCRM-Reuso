package com.miniCRM.miniCRM.dto.tarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * RF10: idCliente e idOportunidade são opcionais e independentes entre si. idUsuario só é aceito
 * de GERENTE/ADMIN, e apenas dentro do escopo de carteira.
 */
public record CriarTarefaRequest(
        @NotBlank @Size(max = 120) String titulo,
        String descricao,
        @NotNull LocalDate dataVencimento,
        Integer idCliente,
        Integer idOportunidade,
        Integer idUsuario) {
}
