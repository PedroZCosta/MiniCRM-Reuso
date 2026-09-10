package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteracaoRepository extends JpaRepository<Interacao, Integer> {
}
