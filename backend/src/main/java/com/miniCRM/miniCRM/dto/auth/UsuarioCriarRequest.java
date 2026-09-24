package com.miniCRM.miniCRM.dto.auth;

import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioCriarRequest(@NotBlank String nome,
                                  @NotBlank @Email String email,
                                  PerfilUsuario perfil) {
}
