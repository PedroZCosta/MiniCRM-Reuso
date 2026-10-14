package com.miniCRM.miniCRM.dto.dashboard;

import java.math.BigDecimal;

public record PosicaoRanking(int posicao, String vendedor, BigDecimal valorGanho, long oportunidadesFechadas) {
}
