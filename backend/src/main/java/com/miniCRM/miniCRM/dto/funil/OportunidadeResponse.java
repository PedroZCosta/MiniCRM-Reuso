package com.miniCRM.miniCRM.dto.funil;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OportunidadeResponse(
        Integer idOportunidade,
        String titulo,
        String cliente,
        BigDecimal valorEstimado,
        String vendedor,
        LocalDate dataPrevista,
        EtapaOportunidade etapa,
        EtapaOportunidade resultado,
        String motivoPerda) {
}
