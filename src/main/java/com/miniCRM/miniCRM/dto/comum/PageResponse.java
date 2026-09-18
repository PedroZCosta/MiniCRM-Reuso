package com.miniCRM.miniCRM.dto.comum;

import java.util.List;

/** Envelope padrão de toda listagem paginada (SPEC-00 §2.1, RF18). */
public record PageResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        int totalPaginas,
        long totalRegistros) {
}
