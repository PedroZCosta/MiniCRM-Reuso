package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class ClienteSpecs {

    private ClienteSpecs() {
    }

    public static Specification<Cliente> naoExcluido() {
        return (root, query, cb) -> cb.isFalse(root.get("excluido"));
    }

    public static Specification<Cliente> excluidos() {
        return (root, query, cb) -> cb.isTrue(root.get("excluido"));
    }

    public static Specification<Cliente> buscaLivre(String termo) {
        return (root, query, cb) -> {
            String padrao = "%" + termo.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nome")), padrao),
                    cb.like(cb.lower(root.get("empresa")), padrao));
        };
    }

    public static Specification<Cliente> comStatus(StatusCliente s) {
        return (root, query, cb) -> cb.equal(root.get("status"), s);
    }

    public static Specification<Cliente> doVendedor(Integer id) {
        return (root, query, cb) -> cb.equal(root.get("vendedor").get("idUsuario"), id);
    }

    public static Specification<Cliente> dentroDoEscopo(List<Integer> vendedores) {
        return (root, query, cb) -> root.get("vendedor").get("idUsuario").in(vendedores);
    }
}
