package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteResumo;

public interface ClienteConsultaService {

    /** 404 se não existir; 422 se excluido=true (não se cria nada para cliente excluído) */
    ClienteResumo buscarAtivo(Integer idCliente);
}
