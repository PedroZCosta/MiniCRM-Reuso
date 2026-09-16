package com.miniCRM.miniCRM.config;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria o administrador inicial no primeiro start (RF32, RN-08). */
@Component
public class SeedAdmin extends SeedBase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public SeedAdmin(UsuarioRepository usuarioRepository,
                     PasswordEncoder passwordEncoder,
                     @Value("${SEED_ADMIN_EMAIL:admin@minicrm.com}") String email,
                     @Value("${SEED_ADMIN_SENHA:Admin123}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    protected boolean jaExecutou() {
        return usuarioRepository.existsByPerfilAndAtivoTrue(PerfilUsuario.ADMIN);
    }

    @Override
    protected void criarDados() {
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(email);
        admin.setSenhaHash(passwordEncoder.encode(senha));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setTrocarSenha(true);
        usuarioRepository.save(admin);
    }

    @Override
    protected String nome() {
        return "admin inicial";
    }
}
