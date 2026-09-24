package com.miniCRM.miniCRM.exception;

/** Id inexistente ou fora do escopo do usuário. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
