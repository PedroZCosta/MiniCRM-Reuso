package com.miniCRM.miniCRM.security;

import com.miniCRM.miniCRM.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaSenhaTest {

    @Test
    @DisplayName("aceita senha com 8 caracteres, letras e numeros")
    void aceitaSenhaForte() {
        assertThatCode(() -> PoliticaSenha.validar("Admin123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("recusa senha com menos de 8 caracteres")
    void recusaSenhaCurta() {
        assertThatThrownBy(() -> PoliticaSenha.validar("Ab1"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("recusa senha sem numero")
    void recusaSenhaSemNumero() {
        assertThatThrownBy(() -> PoliticaSenha.validar("semnumeros"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("recusa senha sem letra")
    void recusaSenhaSemLetra() {
        assertThatThrownBy(() -> PoliticaSenha.validar("12345678"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("recusa senha nula")
    void recusaSenhaNula() {
        assertThatThrownBy(() -> PoliticaSenha.validar(null))
                .isInstanceOf(RegraNegocioException.class);
    }
}
