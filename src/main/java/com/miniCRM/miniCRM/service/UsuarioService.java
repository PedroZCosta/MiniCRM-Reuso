package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.auth.*;
import com.miniCRM.miniCRM.exception.ConflitoException;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.miniCRM.miniCRM.model.enums.PerfilUsuario.*;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioCriarResponse criarUsuario(UsuarioCriarRequest request, Usuario logado) {
        // e-mail unico.
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflitoException("E-mail ja cadastrado");
        }

        // Quem pode criar qual perfil.
        boolean permitido = switch (logado.getPerfil()) {
            case ADMIN -> request.perfil() == ADMIN || request.perfil() == GERENTE;
            case GERENTE -> request.perfil() == VENDEDOR;
            case VENDEDOR -> false;
        };
        if (!permitido) {
            throw new RegraNegocioException(
                    logado.getPerfil() + " nao pode criar usuario com perfil " + request.perfil());
        }

        String senhaProvisoria = gerarSenhaProvisoria();

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        usuario.setSenhaHash(passwordEncoder.encode(senhaProvisoria));
        usuario.setTrocarSenha(true);

        // vendedor criado pelo gerente fica vinculado a ele.
        if (logado.getPerfil() == GERENTE) {
            usuario.setGerente(logado);
        }

        usuarioRepository.save(usuario);
        return new UsuarioCriarResponse(UsuarioResponse.de(usuario), senhaProvisoria);
    }

    // gera a senha com mais de 8 caracteres, com letras e numeros
    private String gerarSenhaProvisoria() {
        return "Crm" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    public UsuarioResponse editarUsuario(Integer id, UsuarioAtualizarRequest usuarioAtualizarRequest) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
        usuario.setNome(usuarioAtualizarRequest.nome());
        usuario.setEmail(usuarioAtualizarRequest.email());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    public void desativarUsuario(Integer id, Usuario logado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));

        // o sistema nao pode ficar sem administrador.
        if (usuario.getIdUsuario().equals(logado.getIdUsuario())) {
            throw new RegraNegocioException("ADMIN nao pode desativar a si mesmo");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void reativarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse alterarPerfil(Integer id, UsuarioPerfilRequest request, Usuario logado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));

        // ninguem muda o proprio perfil, senao o admin se rebaixa e perde o acesso.
        if (usuario.getIdUsuario().equals(logado.getIdUsuario())) {
            throw new RegraNegocioException("Nao e possivel alterar o proprio perfil");
        }

        usuario.setPerfil(request.perfil());

        // so vendedor tem gerente; ao virar outra coisa, o vinculo deixa de valer.
        if (request.perfil() != VENDEDOR) {
            usuario.setGerente(null);
        }

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }




}
