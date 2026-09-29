package com.miniCRM.miniCRM.dto.cliente;

import com.miniCRM.miniCRM.model.enums.StatusCliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Integer idCliente,
        String nome,
        String email,
        String telefone,
        String empresa,
        StatusCliente status,
        VendedorResumo vendedor,
        LocalDateTime criadoEm) {
}
