package com.miniCRM.miniCRM.dto.auth;

import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import jakarta.validation.constraints.NotNull;

public record UsuarioPerfilRequest(@NotNull PerfilUsuario perfil) {
}
