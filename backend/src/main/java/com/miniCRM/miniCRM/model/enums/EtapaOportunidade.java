package com.miniCRM.miniCRM.model.enums;

// RF08: colunas do funil Prospecção, Contato, Proposta e Fechado (= GANHA ou PERDIDA)
public enum EtapaOportunidade {
    PROSPECCAO,
    CONTATO,
    PROPOSTA,
    GANHA,
    PERDIDA;

    public boolean podeIrPara(EtapaOportunidade alvo) {
        if (this.fechada()) return false;
        return alvo != this;
    }

    public boolean fechada() {
        return this == GANHA || this == PERDIDA;
    }
}
