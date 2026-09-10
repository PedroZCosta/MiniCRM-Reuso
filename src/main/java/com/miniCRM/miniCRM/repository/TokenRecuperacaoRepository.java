package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.TokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Integer> {
}
