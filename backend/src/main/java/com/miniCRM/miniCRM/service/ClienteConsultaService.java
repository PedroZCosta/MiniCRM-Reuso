package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteRelatorioLinha;
import com.miniCRM.miniCRM.dto.cliente.ClienteResumo;
import com.miniCRM.miniCRM.model.enums.StatusCliente;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Contrato público da SPEC-02 (SPEC-02 §2.3), consumido pela SPEC-03/04. */
public interface ClienteConsultaService {

    /** 404 se não existir; 422 se excluido=true. */
    ClienteResumo buscarAtivo(Integer idCliente);

    /**
     * Contagem por status, ja recortada pelo escopo (RF15) - consumido pelo DashboardFacade
     * (SPEC-04 §4.1). null = ADMIN = sem filtro. Exclui excluido=true.
     */
    Map<StatusCliente, Long> contarPorStatus(List<Integer> vendedoresOuNull);

    /**
     * Resultado COMPLETO do filtro (RN-06, nunca paginado) - consumido pelo RelatorioService
     * (SPEC-04 §5, RF12). Periodo filtra criadoEm (RF21).
     */
    List<ClienteRelatorioLinha> linhasParaRelatorio(StatusCliente status, LocalDate inicio,
                                                    LocalDate fim, List<Integer> vendedoresOuNull);
}
