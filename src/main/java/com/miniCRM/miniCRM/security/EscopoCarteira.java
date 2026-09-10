package com.miniCRM.miniCRM.security;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Singleton nº 1 do trabalho (SPEC-01 §6): o Spring cria UMA instância
 * (escopo padrão de @Service) e injeta a mesma em todos os módulos.
 *
 * STUB: devolve "sem filtro" até a SPEC-01 implementar com o usuário do JWT.
 * Quem consome (SPEC-02/03/04) já pode compilar e mockar nos testes.
 */
@Service
public class EscopoCarteira {

    /**
     * Ids de vendedor que o usuário logado enxerga (RF15, RF22):
     * VENDEDOR -> [eu] · GERENTE -> [meus vendedores + eu] · ADMIN -> Optional.empty() = sem filtro.
     */
    public Optional<List<Integer>> vendedoresVisiveis() {
        // TODO SPEC-01: ler o usuário autenticado e montar a lista real
        return Optional.empty();
    }
}
