package com.miniCRM.miniCRM.dto.cliente;

import com.miniCRM.miniCRM.model.enums.StatusCliente;

public record ClienteResumo(
        Integer idCliente,
        String nome,
        String email,
        String empresa,
        StatusCliente status) {
}
