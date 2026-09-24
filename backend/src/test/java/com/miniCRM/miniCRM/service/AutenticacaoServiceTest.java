package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.auth.LoginRequest;
import com.miniCRM.miniCRM.exception.CredencialInvalidaException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.exception.UsuarioBloqueadoException;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import com.miniCRM.miniCRM.repository.TokenRecuperacaoRepository;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenRecuperacaoRepository tokenRecuperacaoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    private Usuario usuario;

    @BeforeEach
    void prepararUsuario() {
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNome("Fulano");
        usuario.setEmail("fulano@email.com");
        usuario.setSenhaHash("hash-da-senha-certa");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setTrocarSenha(false);
        usuario.setTentativasLogin(0);
    }

    private LoginRequest login(String senha) {
        return new LoginRequest("fulano@email.com", senha);
    }

    @Test
    @DisplayName("senha errada soma uma tentativa e ainda nao bloqueia")
    void senhaErradaSomaTentativa() {
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> autenticacaoService.login(login("errada")))
                .isInstanceOf(CredencialInvalidaException.class);

        assertThat(usuario.getTentativasLogin()).isEqualTo(1);
        assertThat(usuario.getBloqueadoAte()).isNull();
    }

    @Test
    @DisplayName("a quinta tentativa errada bloqueia por 15 minutos")
    void quintaTentativaBloqueia() {
        usuario.setTentativasLogin(4);
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> autenticacaoService.login(login("errada")))
                .isInstanceOf(CredencialInvalidaException.class);

        assertThat(usuario.getTentativasLogin()).isEqualTo(5);
        assertThat(usuario.getBloqueadoAte()).isAfter(LocalDateTime.now().plusMinutes(14));
    }

    @Test
    @DisplayName("bloqueado nao passa nem com a senha certa, e a senha nem e conferida")
    void bloqueadoNaoPassa() {
        usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> autenticacaoService.login(login("senha-certa")))
                .isInstanceOf(UsuarioBloqueadoException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("bloqueio vencido devolve as 5 tentativas em vez de rebloquear")
    void bloqueioVencidoZeraContador() {
        usuario.setTentativasLogin(5);
        usuario.setBloqueadoAte(LocalDateTime.now().minusMinutes(1));
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> autenticacaoService.login(login("errada")))
                .isInstanceOf(CredencialInvalidaException.class);

        assertThat(usuario.getTentativasLogin()).isEqualTo(1);
        assertThat(usuario.getBloqueadoAte()).isNull();
    }

    @Test
    @DisplayName("e-mail inexistente da a mesma excecao de senha errada")
    void emailInexistente() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacaoService.login(login("qualquer")))
                .isInstanceOf(CredencialInvalidaException.class);
    }

    @Test
    @DisplayName("usuario desativado nao entra")
    void usuarioDesativado() {
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> autenticacaoService.login(login("senha-certa")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("login valido zera tentativas, grava ultimo acesso e devolve o token")
    void loginValido() {
        usuario.setTentativasLogin(3);
        when(usuarioRepository.findByEmail("fulano@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("token-falso");

        var resposta = autenticacaoService.login(login("senha-certa"));

        assertThat(resposta.token()).isEqualTo("token-falso");
        assertThat(usuario.getTentativasLogin()).isZero();
        assertThat(usuario.getUltimoAcesso()).isNotNull();
    }
}
