package com.miniCRM.miniCRM.job;

import com.miniCRM.miniCRM.service.NotificacaoReuniaoService;
import com.miniCRM.miniCRM.service.NotificacaoTarefaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Ator "Schedule" do diagrama de casos de uso (SPEC-04 §3.1): roda ao iniciar e todo dia às 7h.
 * Só dispara a varredura; quem publica os eventos são os services, e a notificação é gravada
 * pelo NotificacaoProcessador.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoJob {

    private final NotificacaoTarefaService notificacaoTarefaService;
    private final NotificacaoReuniaoService notificacaoReuniaoService;

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        executar(LocalDate.now());
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void diariamente() {
        executar(LocalDate.now());
    }

    /** Recebe "hoje" por parâmetro para o teste fixar a data. */
    public void executar(LocalDate hoje) {
        int publicados = notificacaoTarefaService.varrer(hoje) + notificacaoReuniaoService.varrer(hoje);
        log.info("Varredura de notificações de {}: {} evento(s) publicado(s)", hoje, publicados);
    }
}
