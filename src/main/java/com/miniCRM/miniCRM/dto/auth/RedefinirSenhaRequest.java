package com.miniCRM.miniCRM.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @NotBlank @Email String email,
        @NotBlank String codigo,
        @NotBlank String novaSenha) {
}
