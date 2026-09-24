package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.model.Cliente;
import org.springframework.stereotype.Service;

/**
 * Singleton nº 2 do trabalho (SPEC-02 §4): instância única gerenciada pelo Spring.
 * STUB: assinaturas públicas usadas pelos outros módulos. A SPEC-02 implementa.
 */
@Service
public class ClienteService {

    /**
     * Usado pela SPEC-03 ao criar oportunidade e pela SPEC-04 ao criar tarefa.
     * Deve lançar RecursoNaoEncontradoException se não existir
     * e RegraNegocioException se estiver excluído.
     */
    public Cliente buscarAtivo(Integer idCliente) {
        throw new UnsupportedOperationException("implementado na SPEC-02");
    }

    /** Usado pelo listener do RF30 (SPEC-02 §5). PROSPECT -> ATIVO, uma única vez. */
    public void promoverParaAtivoSeProspect(Integer idCliente) {
        throw new UnsupportedOperationException("implementado na SPEC-02");
    }
}
