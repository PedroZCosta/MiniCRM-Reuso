package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByPerfilAndAtivoTrue(PerfilUsuario perfilUsuario);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByGerente(Usuario gerente);

    @Query("""
            select u from Usuario u
            where (:perfil is null or u.perfil = :perfil)
              and (:ativo is null or u.ativo = :ativo)
              and (:semEscopo = true or u.idUsuario in :ids)
            """)
    Page<Usuario> buscar(@Param("perfil") PerfilUsuario perfil,
                         @Param("ativo") Boolean ativo,
                         @Param("semEscopo") boolean semEscopo,
                         @Param("ids") List<Integer> ids,
                         Pageable pageable);

}
