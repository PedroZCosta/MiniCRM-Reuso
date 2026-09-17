package com.miniCRM.miniCRM.exception;

/** Violação de regra RN-xx das specs (SPEC-00 §5). Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
