package com.miniCRM.miniCRM.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceLog implements EmailService {

    @Override
    public void enviarCodigoRecuperacao(String email, String codigo) {
        log.info("[email] codigo de recuperacao para {}: {}", email, codigo);
    }
}
