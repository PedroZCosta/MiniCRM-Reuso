package com.miniCRM.miniCRM.model.enums;

// RF08: colunas do funil Prospecção, Contato, Proposta e Fechado (= GANHA ou PERDIDA)
public enum EtapaOportunidade {
    PROSPECCAO,
    CONTATO,
    PROPOSTA,
    GANHA,
    PERDIDA;

    public boolean fechada() {
        return this == GANHA || this == PERDIDA;
    }
}
