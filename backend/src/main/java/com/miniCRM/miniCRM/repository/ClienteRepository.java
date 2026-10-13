package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>, JpaSpecificationExecutor<Cliente> {

    @Override
    @EntityGraph(attributePaths = "vendedor")
    Page<Cliente> findAll(Specification<Cliente> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "vendedor")
    Optional<Cliente> findById(Integer idCliente);
}
