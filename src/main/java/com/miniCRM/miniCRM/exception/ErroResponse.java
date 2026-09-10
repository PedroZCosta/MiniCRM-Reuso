package com.miniCRM.miniCRM.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de erro da API (SPEC-00 §4). */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes) {

    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, List.of());
    }
}
