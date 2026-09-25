package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.event.OportunidadeFechadaEvent;
import com.miniCRM.miniCRM.event.UsuarioDesativadoEvent;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Observer (lado consumidor) da SPEC-02 §5: status automático do cliente (RF30, RN-06). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteStatusListener {

    private final ClienteService clienteService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoFecharOportunidade(OportunidadeFechadaEvent evento) {
        if (evento.resultado() == EtapaOportunidade.GANHA) {
            clienteService.promoverParaAtivoSeProspect(evento.idCliente());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoDesativarUsuario(UsuarioDesativadoEvent evento) {
        log.info("Usuário {} desativado em {}: carteira preservada, sem responsável ativo na leitura (RF33)",
                evento.idUsuario(), evento.desativadoEm());
    }
}
