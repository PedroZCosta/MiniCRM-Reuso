package com.miniCRM.miniCRM.security;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Singleton nº 1 (SPEC-01 §6). O Spring cria UMA instancia de @Service e injeta a mesma
 * em clientes, funil, tarefas e relatorios: o container controla a instancia unica.
 */
@Service
public class EscopoCarteira {

    private final UsuarioRepository usuarioRepository;

    public EscopoCarteira(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** VENDEDOR -> [eu] · GERENTE -> [meus vendedores + eu] · ADMIN -> vazio, ou seja, sem filtro. */
    public Optional<List<Integer>> vendedoresVisiveis() {
        Usuario logado = usuarioLogado();

        return switch (logado.getPerfil()) {
            case ADMIN -> Optional.empty();

            case GERENTE -> {
                List<Integer> ids = new ArrayList<>();
                ids.add(logado.getIdUsuario());
                usuarioRepository.findByGerente(logado)
                        .forEach(vendedor -> ids.add(vendedor.getIdUsuario()));
                yield Optional.of(ids);
            }

            case VENDEDOR -> Optional.of(List.of(logado.getIdUsuario()));
        };
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
