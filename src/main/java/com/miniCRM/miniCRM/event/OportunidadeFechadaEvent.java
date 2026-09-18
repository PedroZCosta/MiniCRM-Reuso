package com.miniCRM.miniCRM.event;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Publicado pela SPEC-03 ao fechar uma oportunidade; a SPEC-02 escuta para
 * promover o cliente a ATIVO (RF30). Não alterar o payload sem combinar com o grupo.
 */
public record OportunidadeFechadaEvent(
        Integer idOportunidade,
        Integer idCliente,
        Integer idVendedor,
        EtapaOportunidade resultado,
        BigDecimal valorEstimado,
        LocalDateTime fechadaEm) {
}
