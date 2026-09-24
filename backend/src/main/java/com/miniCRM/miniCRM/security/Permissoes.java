package com.miniCRM.miniCRM.security;

import com.miniCRM.miniCRM.model.enums.PerfilUsuario;

import java.util.EnumSet;
import java.util.Set;

public final class Permissoes {

    private Permissoes() {
    }

    public static Set<Permissao> doPerfil(PerfilUsuario perfil) {
        return switch (perfil) {
            case ADMIN -> EnumSet.allOf(Permissao.class);

            // Tudo, menos gerir contas de usuário.
            case GERENTE -> EnumSet.complementOf(EnumSet.of(
                    Permissao.USUARIO_EDITAR,
                    Permissao.USUARIO_DESATIVAR,
                    Permissao.USUARIO_ALTERAR_PERFIL));

            // Só operacionais: sem ranking, relatório, usuários e transferir.
            case VENDEDOR -> EnumSet.of(
                    Permissao.CLIENTE_VER, Permissao.CLIENTE_CRIAR,
                    Permissao.CLIENTE_EDITAR, Permissao.CLIENTE_EXCLUIR,
                    Permissao.INTERACAO_VER, Permissao.INTERACAO_CRIAR,
                    Permissao.OPORTUNIDADE_VER, Permissao.OPORTUNIDADE_CRIAR,
                    Permissao.OPORTUNIDADE_EDITAR, Permissao.OPORTUNIDADE_MOVER,
                    Permissao.OPORTUNIDADE_REABRIR,
                    Permissao.TAREFA_VER, Permissao.TAREFA_CRIAR,
                    Permissao.TAREFA_EDITAR, Permissao.TAREFA_CONCLUIR,
                    Permissao.NOTIFICACAO_VER, Permissao.DASHBOARD_VER);
        };
    }
}
