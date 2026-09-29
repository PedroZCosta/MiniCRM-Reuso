package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.dashboard.DashboardResponse;
import com.miniCRM.miniCRM.dto.funil.TotalEtapa;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Facade (SPEC-04 §4.1): uma chamada, todos os indicadores. Esconde 3 subsistemas (clientes,
 * funil, tarefas) atrás de um ponto único. O escopo de carteira (RF15) é resolvido uma vez aqui e
 * repassado como {@code vendedoresOuNull} (null = ADMIN = sem filtro).
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClienteService clienteService;
    private final OportunidadeService oportunidadeService;
    private final TarefaService tarefaService;
    private final EscopoCarteira escopoCarteira;

    @Transactional(readOnly = true)
    public DashboardResponse montar() {
        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);

        Map<StatusCliente, Long> clientesPorStatus = clienteService.contarPorStatus(vendedores);
        long totalClientes = clientesPorStatus.values().stream().mapToLong(Long::longValue).sum();

        Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa = oportunidadeService.totaisPorEtapa(vendedores);
        Map<EtapaOportunidade, Long> oportunidadesPorEtapa = new EnumMap<>(EtapaOportunidade.class);
        totaisPorEtapa.forEach((etapa, total) -> {
            if (!etapa.fechada()) {
                oportunidadesPorEtapa.put(etapa, total.quantidade());
            }
        });
        long oportunidadesAbertas = oportunidadesPorEtapa.values().stream().mapToLong(Long::longValue).sum();

        return new DashboardResponse(
                totalClientes,
                clientesPorStatus,
                oportunidadesAbertas,
                oportunidadesPorEtapa,
                oportunidadeService.valorEmNegociacao(vendedores),
                tarefaService.alertas(vendedores));
    }
}
