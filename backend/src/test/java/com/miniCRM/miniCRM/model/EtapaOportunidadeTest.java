package com.miniCRM.miniCRM.model;

import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.miniCRM.miniCRM.model.enums.EtapaOportunidade.CONTATO;
import static com.miniCRM.miniCRM.model.enums.EtapaOportunidade.GANHA;
import static com.miniCRM.miniCRM.model.enums.EtapaOportunidade.PERDIDA;
import static com.miniCRM.miniCRM.model.enums.EtapaOportunidade.PROPOSTA;
import static com.miniCRM.miniCRM.model.enums.EtapaOportunidade.PROSPECCAO;
import static org.assertj.core.api.Assertions.assertThat;

class EtapaOportunidadeTest {

    @Test
    @DisplayName("etapa aberta anda para frente e para tras (RF09)")
    void abertaAndaNasDuasDirecoes() {
        assertThat(PROSPECCAO.podeIrPara(PROPOSTA)).isTrue();
        assertThat(PROPOSTA.podeIrPara(CONTATO)).isTrue();
        assertThat(CONTATO.podeIrPara(PROSPECCAO)).isTrue();
    }

    @Test
    @DisplayName("qualquer etapa aberta pode fechar como GANHA ou PERDIDA")
    void abertaPodeFechar() {
        for (EtapaOportunidade etapa : new EtapaOportunidade[] {PROSPECCAO, CONTATO, PROPOSTA}) {
            assertThat(etapa.podeIrPara(GANHA)).isTrue();
            assertThat(etapa.podeIrPara(PERDIDA)).isTrue();
            assertThat(etapa.fechada()).isFalse();
        }
    }

    @Test
    @DisplayName("nao move para a etapa em que ja esta (RN-07)")
    void naoMoveParaAMesmaEtapa() {
        for (EtapaOportunidade etapa : EtapaOportunidade.values()) {
            assertThat(etapa.podeIrPara(etapa)).isFalse();
        }
    }

    @Test
    @DisplayName("fechada nao vai para lugar nenhum, so pelo reabrir (RN-04)")
    void fechadaNaoMove() {
        for (EtapaOportunidade alvo : EtapaOportunidade.values()) {
            assertThat(GANHA.podeIrPara(alvo)).isFalse();
            assertThat(PERDIDA.podeIrPara(alvo)).isFalse();
        }
        assertThat(GANHA.fechada()).isTrue();
        assertThat(PERDIDA.fechada()).isTrue();
    }
}
