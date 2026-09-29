package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.dashboard.RankingResponse;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private OportunidadeService oportunidadeService;
    @Mock
    private EscopoCarteira escopoCarteira;

    @InjectMocks
    private RankingService rankingService;

    private Oportunidade ganha(Integer idVendedor, String vendedorNome, String valor) {
        Usuario vendedor = new Usuario();
        vendedor.setIdUsuario(idVendedor);
        vendedor.setNome(vendedorNome);

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setVendedor(vendedor);
        oportunidade.setEtapa(EtapaOportunidade.GANHA);
        oportunidade.setValorEstimado(new BigDecimal(valor));
        return oportunidade;
    }

    private Oportunidade perdida(Integer idVendedor, String vendedorNome, String valor) {
        Oportunidade oportunidade = ganha(idVendedor, vendedorNome, valor);
        oportunidade.setEtapa(EtapaOportunidade.PERDIDA);
        return oportunidade;
    }

    @Test
    @DisplayName("ordena por valor ganho decrescente")
    void ordenaPorValorGanhoDecrescente() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), any())).thenReturn(List.of(
                ganha(1, "Juliana Torres", "178000.00"),
                ganha(2, "Carlos Souza", "90000.00")));

        RankingResponse resposta = rankingService.montar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 10));

        assertThat(resposta.posicoes()).hasSize(2);
        assertThat(resposta.posicoes().get(0).vendedor()).isEqualTo("Juliana Torres");
        assertThat(resposta.posicoes().get(0).posicao()).isEqualTo(1);
        assertThat(resposta.posicoes().get(1).vendedor()).isEqualTo("Carlos Souza");
    }

    @Test
    @DisplayName("empate em valor desempata por quantidade de fechadas")
    void empateEmValorDesempataPorQuantidadeDeFechadas() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), any())).thenReturn(List.of(
                ganha(1, "Ana", "50000.00"),
                ganha(2, "Bruno", "30000.00"),
                ganha(2, "Bruno", "20000.00")));

        RankingResponse resposta = rankingService.montar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(resposta.posicoes().get(0).vendedor()).isEqualTo("Bruno");
        assertThat(resposta.posicoes().get(0).oportunidadesFechadas()).isEqualTo(2);
        assertThat(resposta.posicoes().get(0).valorGanho()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("oportunidade perdida nao entra no valor ganho")
    void oportunidadePerdidaNaoEntraNoValorGanho() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), any())).thenReturn(List.of(
                ganha(1, "Ana", "50000.00"),
                perdida(1, "Ana", "999999.00")));

        RankingResponse resposta = rankingService.montar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(resposta.posicoes()).hasSize(1);
        assertThat(resposta.posicoes().get(0).valorGanho()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(resposta.posicoes().get(0).oportunidadesFechadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("vendedores com o mesmo nome continuam em posicoes separadas")
    void vendedoresComOMesmoNomeContinuamSeparados() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), any())).thenReturn(List.of(
                ganha(1, "Ana Lima", "50000.00"),
                ganha(2, "Ana Lima", "30000.00")));

        RankingResponse resposta = rankingService.montar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(resposta.posicoes()).hasSize(2);
        assertThat(resposta.posicoes().get(0).valorGanho()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(resposta.posicoes().get(1).valorGanho()).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    @DisplayName("gerente ve somente os vendedores do escopo")
    void gerenteVeSomenteOsVendedoresDoEscopo() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.of(List.of(2, 7)));
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), eq(List.of(2, 7)))).thenReturn(List.of());

        rankingService.montar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        verify(oportunidadeService).fechadasNoPeriodo(any(), any(), eq(List.of(2, 7)));
    }

    @Test
    @DisplayName("periodo omitido usa o mes corrente")
    void periodoOmitidoUsaOMesCorrente() {
        when(escopoCarteira.vendedoresVisiveis()).thenReturn(Optional.empty());
        when(oportunidadeService.fechadasNoPeriodo(any(), any(), any())).thenReturn(List.of());

        RankingResponse resposta = rankingService.montar(null, null);

        LocalDate hoje = LocalDate.now();
        assertThat(resposta.periodo().inicio()).isEqualTo(hoje.withDayOfMonth(1));
        assertThat(resposta.periodo().fim()).isEqualTo(hoje);
    }

    @Test
    @DisplayName("data inicial maior que a final e regra de negocio")
    void dataInicialMaiorQueFinalEhRegraDeNegocio() {
        assertThatThrownBy(() -> rankingService.montar(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(RegraNegocioException.class);
    }
}
