package com.miniCRM.miniCRM.exception;

/** Ação negada por perfil ou fora do escopo de carteira. Vira HTTP 403. */
public class SemPermissaoException extends RuntimeException {
    public SemPermissaoException(String mensagem) {
        super(mensagem);
    }
}
