package com.miniCRM.miniCRM.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TrocarSenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank String novaSenha) {
}
