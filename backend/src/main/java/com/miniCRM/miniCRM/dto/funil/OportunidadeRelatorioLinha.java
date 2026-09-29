package com.miniCRM.miniCRM.dto.funil;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha do relatorio de oportunidades (RF12/RN-07), ja materializada dentro do modulo funil. */
public record OportunidadeRelatorioLinha(
        String cliente,
        BigDecimal valorEstimado,
        EtapaOportunidade etapa,
        LocalDate dataPrevista,
        String vendedorResponsavel) {
}
