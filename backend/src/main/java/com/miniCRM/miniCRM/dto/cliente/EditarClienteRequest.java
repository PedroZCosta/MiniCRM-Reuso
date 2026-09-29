package com.miniCRM.miniCRM.dto.cliente;

public record EditarClienteRequest(
        String nome,
        String email,
        String telefone,
        String empresa) {
}
