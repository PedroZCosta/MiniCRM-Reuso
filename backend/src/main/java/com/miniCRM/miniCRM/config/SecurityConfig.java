package com.miniCRM.miniCRM.config;


import com.miniCRM.miniCRM.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Segurança da API. Este bean desliga a auto-config do Spring Boot. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        // CSRF só faz sentido com cookie automático; nosso JWT vai no header.
        http.csrf(csrf -> csrf.disable());

        // Sem sessão no servidor: a identidade vem do JWT a cada requisição.
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                // Listados um a um: com /auth/** um endpoint novo nasceria público.
                .requestMatchers("/api/v1/auth/login",
                        "/api/v1/auth/recuperar-senha",
                        "/api/v1/auth/redefinir-senha",
                        "/error").permitAll()
                .anyRequest().authenticated());

        // Login de formulário e basic não servem para API REST.
        http.httpBasic(basic -> basic.disable());
        http.formLogin(form -> form.disable());

        // Nosso filtro roda antes da decisão de autorização, senão o usuário chega anônimo.
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Sem isso, acesso negado vira 403 em vez de 401.
        http.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authEx) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(
                "{\"status\":401,\"erro\":\"NAO_AUTORIZADO\",\"mensagem\":\"Token ausente ou inválido\"}");
        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
