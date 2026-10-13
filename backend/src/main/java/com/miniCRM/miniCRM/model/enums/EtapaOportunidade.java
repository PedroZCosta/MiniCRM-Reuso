package com.miniCRM.miniCRM.model.enums;

import java.util.Set;

// RF08: colunas do funil Prospecção, Contato, Proposta e Fechado (= GANHA ou PERDIDA)
// padrão State: cada etapa sabe para onde pode ir. Context = Oportunidade, State = esta enum.
public enum EtapaOportunidade {
    PROSPECCAO {
        Set<EtapaOportunidade> proximas() {
            return Set.of(CONTATO, PROPOSTA, GANHA, PERDIDA);
        }
    },
    CONTATO {
        Set<EtapaOportunidade> proximas() {
            return Set.of(PROSPECCAO, PROPOSTA, GANHA, PERDIDA);
        }
    },
    PROPOSTA {
        Set<EtapaOportunidade> proximas() {
            return Set.of(PROSPECCAO, CONTATO, GANHA, PERDIDA);
        }
    },
    // fechada so sai pelo reabrir
    GANHA {
        Set<EtapaOportunidade> proximas() {
            return Set.of();
        }

        @Override
        public boolean fechada() {
            return true;
        }
    },
    PERDIDA {
        Set<EtapaOportunidade> proximas() {
            return Set.of();
        }

        @Override
        public boolean fechada() {
            return true;
        }
    };

    abstract Set<EtapaOportunidade> proximas();

    public boolean fechada() {
        return false;
    }

    public boolean podeIrPara(EtapaOportunidade alvo) {
        return proximas().contains(alvo);
    }
}
