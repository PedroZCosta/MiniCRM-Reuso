package com.miniCRM.miniCRM.model.enums;

// RF28/RN-03: sempre calculada a partir de concluida + dataVencimento, nunca persistida.
public enum SituacaoTarefa {
    PENDENTE,
    VENCIDA,
    CONCLUIDA
}
