package com.miniCRM.miniCRM.dto.funil;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarOportunidadeRequest(
        Integer idCliente,
        String titulo,
        BigDecimal valorEstimado,
        LocalDate dataPrevista,
        Integer idVendedor) {
}
