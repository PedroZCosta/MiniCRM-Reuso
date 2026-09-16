package com.miniCRM.miniCRM.repository;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByPerfilAndAtivoTrue(PerfilUsuario perfilUsuario);
}
