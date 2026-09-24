package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.auth.LoginRequest;
import com.miniCRM.miniCRM.dto.auth.RecuperarSenhaRequest;
import com.miniCRM.miniCRM.dto.auth.RedefinirSenhaRequest;
import com.miniCRM.miniCRM.dto.auth.TrocarSenhaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.miniCRM.miniCRM.model.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import com.miniCRM.miniCRM.dto.auth.LoginResponse;
import com.miniCRM.miniCRM.service.AutenticacaoService;
import com.miniCRM.miniCRM.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    private final JwtService jwtService;

    public AutenticacaoController(AutenticacaoService autenticacaoService, JwtService jwtService) {
        this.autenticacaoService = autenticacaoService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return autenticacaoService.login(loginRequest);
    }

    @PutMapping("/trocar-senha")
    public void trocarSenha(@Valid @RequestBody TrocarSenhaRequest request,
                            @AuthenticationPrincipal Usuario logado) {
        autenticacaoService.trocarSenha(request, logado);
    }

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        autenticacaoService.recuperarSenha(request);
    }


    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        autenticacaoService.redefinirSenha(request);
    }
}
