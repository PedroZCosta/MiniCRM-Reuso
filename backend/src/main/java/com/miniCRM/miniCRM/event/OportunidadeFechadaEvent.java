package com.miniCRM.miniCRM.event;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Único evento do sistema. SPEC-03 publica ao fechar uma
 * oportunidade; SPEC-02 escuta para promover o cliente a ATIVO.
 * Não alterar o payload sem combinar com o grupo.
 */
public record OportunidadeFechadaEvent(
        Integer idOportunidade,
        Integer idCliente,
        Integer idVendedor,
        EtapaOportunidade resultado,
        BigDecimal valorEstimado,
        LocalDateTime fechadaEm) {
}
