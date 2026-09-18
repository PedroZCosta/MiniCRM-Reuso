package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.HistoricoItem;

import java.util.List;

/** Contrato público da SPEC-03 (SPEC-02 §2.3), consumido pelo histórico do cliente. */
public interface OportunidadeConsultaService {

    List<HistoricoItem> historicoDoCliente(Integer idCliente);
}
