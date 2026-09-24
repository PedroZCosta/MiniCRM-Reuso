package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final int HORAS_VALIDADE = 8;

    private final SecretKey chave;

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        // objeto de chave que o HS256 aceita (valida o minimo de 32 bytes)
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expira = new Date(agora.getTime() + HORAS_VALIDADE * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(agora)
                .expiration(expira)
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public String extrairIdUsuario(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


}
