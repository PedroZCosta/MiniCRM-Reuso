package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.auth.LoginRequest;
import com.miniCRM.miniCRM.dto.auth.LoginResponse;
import com.miniCRM.miniCRM.dto.auth.UsuarioResponse;
import com.miniCRM.miniCRM.exception.CredencialInvalidaException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.exception.UsuarioBloqueadoException;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AutenticacaoService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final int HORAS_TOKEN = 8;

    // Mesma mensagem para e-mail inexistente e senha errada: não revela quem tem conta.
    private static final String CREDENCIAL_INVALIDA = "E-mail ou senha inválidos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email()) // Procura email
                .orElseThrow(() -> new CredencialInvalidaException(CREDENCIAL_INVALIDA));

        aplicarBloqueio(usuario); // verifica se usuario está bloqueado

        if (!usuario.getAtivo()) { // Se usuario não ativo
            throw new RegraNegocioException("Usuário desativado");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) { // Se senha errada
            registrarTentativaInvalida(usuario);
            throw new CredencialInvalidaException(CREDENCIAL_INVALIDA);
        }

        registrarAcessoValido(usuario);

        return new LoginResponse(
                "TOKEN_PENDENTE",   // trocar pela chamada ao JwtService
                LocalDateTime.now().plusHours(HORAS_TOKEN),
                UsuarioResponse.de(usuario));
    }

    // Aplica bloqueio caso tenha tentativas excedidas
    private void aplicarBloqueio(Usuario usuario) {
        if (usuario.getBloqueadoAte() == null) {
            return;
        }
        if (usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            throw new UsuarioBloqueadoException("Usuário bloqueado por excesso de tentativas");
        }
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
    }

    private void registrarTentativaInvalida(Usuario usuario) {
        usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        if (usuario.getTentativasLogin() >= MAX_TENTATIVAS) {
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
        }
        usuarioRepository.save(usuario);
    }

    private void registrarAcessoValido(Usuario usuario) {
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
}
