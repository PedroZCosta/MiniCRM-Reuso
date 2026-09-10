package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OportunidadeRepository extends JpaRepository<Oportunidade, Integer> {
}
