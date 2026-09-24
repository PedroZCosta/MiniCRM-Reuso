package com.miniCRM.miniCRM.dto.funil;

import java.math.BigDecimal;

/** Contagem e soma de uma etapa do funil (RF19). */
public record TotalEtapa(long quantidade, BigDecimal valorTotal) {
}
