package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.dashboard.PeriodoRanking;
import com.miniCRM.miniCRM.dto.dashboard.PosicaoRanking;
import com.miniCRM.miniCRM.dto.dashboard.RankingResponse;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * RF31: ranking de vendedores por valor ganho no período. valorGanho é a soma de valorEstimado das
 * oportunidades GANHA e oportunidadesFechadas é a quantidade de GANHA: PERDIDA fica de fora para o
 * desempate não premiar quem perde mais. Vendedor sem GANHA no período não aparece. O agrupamento
 * é por id, porque dois vendedores podem ter o mesmo nome.
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private final OportunidadeService oportunidadeService;
    private final EscopoCarteira escopoCarteira;

    @Transactional(readOnly = true)
    public RankingResponse montar(LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        LocalDate de = inicio != null ? inicio : hoje.withDayOfMonth(1);
        LocalDate ate = fim != null ? fim : hoje;

        if (de.isAfter(ate)) {
            throw new RegraNegocioException("A data inicial não pode ser maior que a data final");
        }

        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);

        Map<Integer, List<Oportunidade>> ganhasPorVendedor = oportunidadeService
                .fechadasNoPeriodo(de, ate, vendedores).stream()
                .filter(o -> o.getEtapa() == EtapaOportunidade.GANHA)
                .collect(Collectors.groupingBy(o -> o.getVendedor().getIdUsuario()));

        Comparator<PosicaoRanking> ordem = Comparator
                .comparing(PosicaoRanking::valorGanho, Comparator.reverseOrder())
                .thenComparing(PosicaoRanking::oportunidadesFechadas, Comparator.reverseOrder())
                .thenComparing(PosicaoRanking::vendedor);

        List<PosicaoRanking> semPosicao = ganhasPorVendedor.values().stream()
                .map(ganhas -> new PosicaoRanking(0, ganhas.get(0).getVendedor().getNome(),
                        somar(ganhas), ganhas.size()))
                .sorted(ordem)
                .toList();

        List<PosicaoRanking> posicoes = IntStream.range(0, semPosicao.size())
                .mapToObj(i -> new PosicaoRanking(i + 1, semPosicao.get(i).vendedor(),
                        semPosicao.get(i).valorGanho(), semPosicao.get(i).oportunidadesFechadas()))
                .toList();

        return new RankingResponse(new PeriodoRanking(de, ate), posicoes);
    }

    private BigDecimal somar(List<Oportunidade> lista) {
        return lista.stream().map(Oportunidade::getValorEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
