package com.miniCRM.miniCRM.security;

import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import com.miniCRM.miniCRM.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String id = jwtService.extrairIdUsuario(token);

                Usuario usuario = usuarioRepository.findById(Integer.valueOf(id)).orElse(null);

                if (usuario != null && usuario.getAtivo()) {
                    List<SimpleGrantedAuthority> authorities = Permissoes.doPerfil(usuario.getPerfil())
                            .stream()
                            .map(p -> new SimpleGrantedAuthority(p.name()))
                            .toList();

                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException e) {
                // token invalido ou expirado: segue sem autenticar
            }
        }

        chain.doFilter(request, response);
    }
}
