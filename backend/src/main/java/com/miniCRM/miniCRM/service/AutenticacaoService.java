package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.auth.LoginRequest;
import com.miniCRM.miniCRM.dto.auth.RecuperarSenhaRequest;
import com.miniCRM.miniCRM.dto.auth.RedefinirSenhaRequest;
import com.miniCRM.miniCRM.dto.auth.TrocarSenhaRequest;
import com.miniCRM.miniCRM.dto.auth.LoginResponse;
import com.miniCRM.miniCRM.dto.auth.UsuarioResponse;
import com.miniCRM.miniCRM.exception.CredencialInvalidaException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.exception.UsuarioBloqueadoException;
import com.miniCRM.miniCRM.model.TokenRecuperacao;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.security.PoliticaSenha;
import com.miniCRM.miniCRM.repository.TokenRecuperacaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AutenticacaoService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final int HORAS_TOKEN = 8;
    private static final int HORAS_CODIGO = 1;

    // mesma mensagem para codigo errado, expirado ou ja usado.
    private static final String CODIGO_INVALIDO = "Codigo invalido ou expirado";

    // Mesma mensagem para e-mail inexistente e senha errada: não revela quem tem conta.
    private static final String CREDENCIAL_INVALIDA = "E-mail ou senha inválidos";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               TokenRecuperacaoRepository tokenRecuperacaoRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
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
                jwtService.gerarToken(usuario), // Gera o token
                LocalDateTime.now().plusHours(HORAS_TOKEN), // Expira em 8 horas
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

    public void trocarSenha(TrocarSenhaRequest request, Usuario logado) {
        Usuario usuario = usuarioRepository.findById(logado.getIdUsuario())
                .orElseThrow(() -> new CredencialInvalidaException(CREDENCIAL_INVALIDA));

        // so troca quem sabe a senha atual, mesmo ja estando logado.
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new CredencialInvalidaException("Senha atual incorreta");
        }

        PoliticaSenha.validar(request.novaSenha());

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setTrocarSenha(false);
        usuarioRepository.save(usuario);
    }

    public void recuperarSenha(RecuperarSenhaRequest request) {
        // se o e-mail nao existe, sai calado: responder diferente revelaria quem tem conta.
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(request.email());
        if (encontrado.isEmpty()) {
            return;
        }
        Usuario usuario = encontrado.get();

        // codigo novo invalida os anteriores que ainda nao foram usados.
        List<TokenRecuperacao> pendentes = tokenRecuperacaoRepository.findByUsuarioAndUsadoFalse(usuario);
        pendentes.forEach(t -> t.setUsado(true));
        tokenRecuperacaoRepository.saveAll(pendentes);

        String codigo = gerarCodigo();

        TokenRecuperacao token = new TokenRecuperacao();
        token.setUsuario(usuario);
        token.setCodigoHash(passwordEncoder.encode(codigo));
        token.setExpiraEm(LocalDateTime.now().plusHours(HORAS_CODIGO));
        token.setUsado(false);
        tokenRecuperacaoRepository.save(token);

        emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo);
    }

    public void redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RegraNegocioException(CODIGO_INVALIDO));

        // o codigo fica no banco so como hash, entao a comparacao e a mesma da senha.
        TokenRecuperacao token = tokenRecuperacaoRepository.findByUsuarioAndUsadoFalse(usuario).stream()
                .filter(t -> passwordEncoder.matches(request.codigo(), t.getCodigoHash()))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException(CODIGO_INVALIDO));

        if (token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException(CODIGO_INVALIDO);
        }

        PoliticaSenha.validar(request.novaSenha());

        token.setUsado(true);
        tokenRecuperacaoRepository.save(token);

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setTrocarSenha(false);
        // quem redefiniu a senha merece sair do bloqueio.
        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }

    // seis digitos, com zeros a esquerda quando o sorteio for baixo.
    private String gerarCodigo() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
