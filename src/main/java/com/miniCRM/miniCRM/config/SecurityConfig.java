package com.miniCRM.miniCRM.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/** Segurança da API (SPEC-01). Este bean desliga a auto-config do Spring Boot. */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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

        return http.build();
    }
}
