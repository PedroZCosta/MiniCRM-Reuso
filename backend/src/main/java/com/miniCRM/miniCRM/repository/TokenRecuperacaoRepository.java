package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.TokenRecuperacao;
import com.miniCRM.miniCRM.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Integer> {

    List<TokenRecuperacao> findByUsuarioAndUsadoFalse(Usuario usuario);
}
