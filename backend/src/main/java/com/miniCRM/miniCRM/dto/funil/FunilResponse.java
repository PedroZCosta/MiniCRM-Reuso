package com.miniCRM.miniCRM.dto.funil;

import java.math.BigDecimal;
import java.util.List;

public record FunilResponse(
        List<ColunaFunilResponse> colunas,
        BigDecimal valorEmNegociacao) {
}
