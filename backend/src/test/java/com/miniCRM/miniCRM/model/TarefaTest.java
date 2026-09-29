package com.miniCRM.miniCRM.model;

import com.miniCRM.miniCRM.model.enums.SituacaoTarefa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** RF28: "vencida" nunca e coluna - mudar o relogio do teste muda a situacao. */
class TarefaTest {

    @Test
    @DisplayName("tarefa nao esta vencida no dia do vencimento e fica vencida no dia seguinte")
    void tarefaNaoEstaVencidaNoDiaDoVencimentoEFicaVencidaNoDiaSeguinte() {
        Tarefa tarefa = new Tarefa();
        tarefa.setDataVencimento(LocalDate.of(2026, 9, 10));
        tarefa.setConcluida(false);

        assertThat(tarefa.vencida(LocalDate.of(2026, 9, 10))).isFalse();
        assertThat(tarefa.situacao(LocalDate.of(2026, 9, 10))).isEqualTo(SituacaoTarefa.PENDENTE);

        assertThat(tarefa.vencida(LocalDate.of(2026, 9, 11))).isTrue();
        assertThat(tarefa.situacao(LocalDate.of(2026, 9, 11))).isEqualTo(SituacaoTarefa.VENCIDA);
    }

    @Test
    @DisplayName("tarefa concluida nunca esta vencida mesmo com o vencimento no passado")
    void tarefaConcluidaNuncaEstaVencida() {
        Tarefa tarefa = new Tarefa();
        tarefa.setDataVencimento(LocalDate.of(2026, 1, 1));
        tarefa.setConcluida(true);

        assertThat(tarefa.vencida(LocalDate.of(2026, 9, 10))).isFalse();
        assertThat(tarefa.situacao(LocalDate.of(2026, 9, 10))).isEqualTo(SituacaoTarefa.CONCLUIDA);
    }

    @Test
    @DisplayName("situacao pendente quando aberta e dentro do prazo")
    void situacaoPendenteQuandoAbertaEDentroDoPrazo() {
        Tarefa tarefa = new Tarefa();
        tarefa.setDataVencimento(LocalDate.of(2026, 12, 31));
        tarefa.setConcluida(false);

        assertThat(tarefa.situacao(LocalDate.of(2026, 9, 10))).isEqualTo(SituacaoTarefa.PENDENTE);
    }
}
