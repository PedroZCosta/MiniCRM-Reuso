package com.miniCRM.miniCRM.dto.funil;

import java.util.List;

public record OportunidadeDetalheResponse(
        OportunidadeResponse oportunidade,
        List<HistoricoEtapaResponse> historico) {
}
