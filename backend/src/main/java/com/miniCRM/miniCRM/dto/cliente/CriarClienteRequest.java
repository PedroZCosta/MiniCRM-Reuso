package com.miniCRM.miniCRM.dto.cliente;

public record CriarClienteRequest(
        String nome,
        String email,
        String telefone,
        String empresa,
        Integer idVendedor) {
}
