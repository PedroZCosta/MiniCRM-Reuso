package com.miniCRM.miniCRM.security;

import com.miniCRM.miniCRM.exception.RegraNegocioException;

/** Regra unica de senha forte. Vale para criar, trocar e redefinir. */
public final class PoliticaSenha {

    private PoliticaSenha() {
    }

    public static void validar(String senha) {
        // minimo 8 caracteres, com pelo menos uma letra e um numero.
        boolean tamanhoOk = senha != null && senha.length() >= 8;
        boolean temLetra = senha != null && senha.chars().anyMatch(Character::isLetter);
        boolean temNumero = senha != null && senha.chars().anyMatch(Character::isDigit);

        if (!tamanhoOk || !temLetra || !temNumero) {
            throw new RegraNegocioException("A senha precisa ter no minimo 8 caracteres, com letras e numeros");
        }
    }
}
