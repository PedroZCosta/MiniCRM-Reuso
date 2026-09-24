package com.miniCRM.miniCRM.dto.funil;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

import java.time.LocalDateTime;

public record HistoricoEtapaResponse(
        EtapaOportunidade etapaAnterior,
        EtapaOportunidade etapaNova,
        String usuario,
        LocalDateTime alteradoEm) {
}
