package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.funil.TotalEtapa;
import com.miniCRM.miniCRM.model.Oportunidade;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * STUB: assinaturas públicas usadas pela SPEC-02 (histórico) e SPEC-04
 * (dashboard, ranking, relatórios). A SPEC-03 implementa.
 * vendedoresOuNull segue o EscopoCarteira: null = sem filtro (ADMIN).
 */
@Service
public class OportunidadeService {

    public List<Oportunidade> listarDoCliente(Integer idCliente) {
        throw new UnsupportedOperationException("implementado na SPEC-03");
    }

    public Map<EtapaOportunidade, TotalEtapa> totaisPorEtapa(List<Integer> vendedoresOuNull) {
        throw new UnsupportedOperationException("implementado na SPEC-03");
    }

    public BigDecimal valorEmNegociacao(List<Integer> vendedoresOuNull) {
        throw new UnsupportedOperationException("implementado na SPEC-03");
    }

    public List<Oportunidade> fechadasNoPeriodo(LocalDate inicio, LocalDate fim,
                                                List<Integer> vendedoresOuNull) {
        throw new UnsupportedOperationException("implementado na SPEC-03");
    }
}
