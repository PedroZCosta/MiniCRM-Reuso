package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.MotivoPerda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotivoPerdaRepository extends JpaRepository<MotivoPerda, Short> {
}
