package com.miniCRM.miniCRM.dto.auth;

// Resposta do POST /usuarios. A senha provisória aparece uma única vez
public record UsuarioCriarResponse(
        UsuarioResponse usuario,
        String senhaProvisoria) {
}
