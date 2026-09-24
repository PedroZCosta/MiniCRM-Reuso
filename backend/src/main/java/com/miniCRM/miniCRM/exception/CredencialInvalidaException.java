package com.miniCRM.miniCRM.exception;

/** E-mail ou senha errados. Mensagem sempre genérica (SPEC-01 §2). Vira HTTP 401. */
public class CredencialInvalidaException extends RuntimeException {
    public CredencialInvalidaException(String mensagem) {
        super(mensagem);
    }
}
