package com.miniCRM.miniCRM.dto.cliente;

import com.miniCRM.miniCRM.model.enums.StatusCliente;

/**
 * Linha do relatorio de clientes (RF12/RN-07). Materializada dentro do modulo cliente para
 * nao vazar entidade lazy com spring.jpa.open-in-view=false.
 */
public record ClienteRelatorioLinha(
        String nome,
        String email,
        String telefone,
        String empresa,
        StatusCliente status,
        String vendedorResponsavel) {
}
