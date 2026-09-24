package com.miniCRM.miniCRM.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioAtualizarRequest(
        @NotBlank String nome,
        @NotBlank @Email String email) {
}

