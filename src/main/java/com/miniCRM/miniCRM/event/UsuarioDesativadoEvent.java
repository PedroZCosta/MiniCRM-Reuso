package com.miniCRM.miniCRM.event;

import java.time.LocalDateTime;

/**
 * Publicado pela SPEC-01 ao desativar um usuário (RF33). Payload da SPEC-00 §6.
 * Não alterar sem combinar com o grupo.
 */
public record UsuarioDesativadoEvent(Integer idUsuario, LocalDateTime desativadoEm) {
}
