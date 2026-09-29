package com.miniCRM.miniCRM.exception;

/** Parametro de requisicao invalido (ex.: formato de relatorio inexistente). Vira HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
