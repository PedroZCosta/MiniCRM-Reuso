package com.miniCRM.miniCRM.dto.funil;

import java.math.BigDecimal;
import java.util.List;

public record ColunaFunilResponse(
        String etapa,
        long quantidade,
        BigDecimal valorTotal,
        List<OportunidadeResponse> oportunidades) {
}
