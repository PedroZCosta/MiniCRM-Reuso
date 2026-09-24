package com.miniCRM.miniCRM.dto.auth;

import java.time.LocalDateTime;

// Resposta do POST /api/v1/auth/login
public record LoginResponse(
        String token,
        LocalDateTime expiraEm,
        UsuarioResponse usuario) {
}
