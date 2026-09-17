package com.miniCRM.miniCRM.security;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Contrato público da SPEC-01 (SPEC-00 §1, Canal 2), consumido por SPEC-02/03/04.
 * STUB: devolve "sem filtro" até a SPEC-01 ler o usuário do JWT.
 */
@Service
public class EscopoCarteira {

    /**
     * Ids de vendedor que o usuário logado enxerga (RF15, RF22):
     * VENDEDOR -> [eu] · GERENTE -> [meus vendedores + eu] · ADMIN -> Optional.empty() = sem filtro.
     */
    public Optional<List<Integer>> vendedoresVisiveis() {
        return Optional.empty();
    }
}
