package com.miniCRM.miniCRM.dto.auth;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;

// Usuário como a API devolve. Nunca expõe senhaHash nem controle de bloqueio.
public record UsuarioResponse(
        Integer idUsuario, String nome, String email,
        PerfilUsuario perfil, Boolean ativo, Boolean trocarSenha) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), usuario.getAtivo(), usuario.getTrocarSenha());
    }
}
