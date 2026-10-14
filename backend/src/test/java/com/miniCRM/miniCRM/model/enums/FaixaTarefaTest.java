package com.miniCRM.miniCRM.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FaixaTarefaTest {

    private final LocalDate hoje = LocalDate.of(2026, 9, 10);

    @Test
    @DisplayName("classifica vencimento passado como vencida")
    void classificaAtrasadaComoVencida() {
        assertThat(FaixaTarefa.classificarParaNotificacao(hoje.minusDays(3), hoje))
                .isEqualTo(FaixaTarefa.VENCIDA);
    }

    @Test
    @DisplayName("classifica vencimento de hoje como hoje")
    void classificaVencimentoDeHojeComoHoje() {
        assertThat(FaixaTarefa.classificarParaNotificacao(hoje, hoje)).isEqualTo(FaixaTarefa.HOJE);
    }

    @Test
    @DisplayName("classifica vencimento de amanha como d1")
    void classificaAmanhaComoD1() {
        assertThat(FaixaTarefa.classificarParaNotificacao(hoje.plusDays(1), hoje))
                .isEqualTo(FaixaTarefa.D1);
    }

    @Test
    @DisplayName("classifica vencimento de quinze dias como d15")
    void classificaQuinzeDiasComoD15() {
        assertThat(FaixaTarefa.classificarParaNotificacao(hoje.plusDays(15), hoje))
                .isEqualTo(FaixaTarefa.D15);
    }

    @Test
    @DisplayName("nao classifica data fora das quatro faixas")
    void naoClassificaDataForaDasFaixas() {
        assertThat(FaixaTarefa.classificarParaNotificacao(hoje.plusDays(7), hoje)).isNull();
    }

    @Test
    @DisplayName("janela da faixa vencida termina ontem")
    void janelaDaFaixaVencidaTerminaOntem() {
        assertThat(FaixaTarefa.VENCIDA.inicio(hoje)).isNull();
        assertThat(FaixaTarefa.VENCIDA.fim(hoje)).isEqualTo(hoje.minusDays(1));
    }

    @Test
    @DisplayName("janela da faixa d15 vai de hoje mais dois ate hoje mais quinze")
    void janelaDaFaixaD15VaiDeHojeMaisDoisAteHojeMaisQuinze() {
        assertThat(FaixaTarefa.D15.inicio(hoje)).isEqualTo(hoje.plusDays(2));
        assertThat(FaixaTarefa.D15.fim(hoje)).isEqualTo(hoje.plusDays(15));
    }

    @Test
    @DisplayName("cada faixa tem o tipo de notificacao correspondente")
    void cadaFaixaTemOTipoDeNotificacaoCorrespondente() {
        assertThat(FaixaTarefa.D15.tipoNotificacao()).isEqualTo(TipoNotificacao.D15);
        assertThat(FaixaTarefa.D1.tipoNotificacao()).isEqualTo(TipoNotificacao.D1);
        assertThat(FaixaTarefa.HOJE.tipoNotificacao()).isEqualTo(TipoNotificacao.HOJE);
        assertThat(FaixaTarefa.VENCIDA.tipoNotificacao()).isEqualTo(TipoNotificacao.VENCIDA);
    }
}
