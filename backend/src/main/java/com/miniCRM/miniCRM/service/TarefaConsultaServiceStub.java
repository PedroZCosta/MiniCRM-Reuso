package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.comum.HistoricoItem;
import org.springframework.stereotype.Service;

import java.util.List;

/** STUB: até a SPEC-04 substituir por uma implementação real. */
@Service
public class TarefaConsultaServiceStub implements TarefaConsultaService {

    @Override
    public List<HistoricoItem> historicoDoCliente(Integer idCliente) {
        throw new UnsupportedOperationException("implementado na SPEC-04");
    }
}
