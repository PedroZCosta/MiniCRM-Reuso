package com.miniCRM.miniCRM.exception;

/** Estado conflitante: e-mail duplicado, transição de etapa inválida. Vira HTTP 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
