package com.miniCRM.miniCRM.exception;

/** Usuário bloqueado por excesso de tentativas (RN-03). Vira HTTP 423. */
public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException(String mensagem) {
        super(mensagem);
    }
}
