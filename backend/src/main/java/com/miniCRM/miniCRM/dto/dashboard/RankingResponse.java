package com.miniCRM.miniCRM.dto.dashboard;

import java.util.List;

/** RF31. */
public record RankingResponse(PeriodoRanking periodo, List<PosicaoRanking> posicoes) {
}
