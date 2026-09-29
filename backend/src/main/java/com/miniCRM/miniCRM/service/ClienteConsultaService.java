package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteResumo;

/** Contrato público da SPEC-02 (SPEC-02 §2.3), consumido pela SPEC-03/04. */
public interface ClienteConsultaService {

    /** 404 se não existir; 422 se excluido=true. */
    ClienteResumo buscarAtivo(Integer idCliente);
}
