package com.miniCRM.miniCRM.service;

/** Envio de e-mail. Hoje so loga; a implementacao SMTP entra depois sem mexer em quem chama. */
public interface EmailService {
    void enviarCodigoRecuperacao(String email, String codigo);
}
