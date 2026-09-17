package com.miniCRM.miniCRM.exception;

/** Id inexistente ou fora do escopo de carteira do usuário (SPEC-00 §5). Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
