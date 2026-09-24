package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.HistoricoEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoEtapaRepository extends JpaRepository<HistoricoEtapa, Integer> {

    List<HistoricoEtapa> findByOportunidadeIdOportunidadeOrderByIdHistoricoAsc(Integer idOportunidade);
}
