package com.miniCRM.miniCRM.dto.funil;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditarOportunidadeRequest(
        String titulo,
        BigDecimal valorEstimado,
        LocalDate dataPrevista) {
}
