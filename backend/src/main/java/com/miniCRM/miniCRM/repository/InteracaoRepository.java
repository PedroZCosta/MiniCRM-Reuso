package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Interacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteracaoRepository extends JpaRepository<Interacao, Integer> {

    Page<Interacao> findByClienteIdClienteOrderByDataInteracaoDesc(Integer idCliente, Pageable pageable);

    List<Interacao> findByClienteIdCliente(Integer idCliente);
}
