package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.auth.LoginRequest;
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






}
