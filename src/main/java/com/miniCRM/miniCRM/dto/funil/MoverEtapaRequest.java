package com.miniCRM.miniCRM.dto.funil;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;

public record MoverEtapaRequest(
        EtapaOportunidade novaEtapa,
        Short idMotivoPerda) {
}
