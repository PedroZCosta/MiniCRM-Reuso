package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.notificacao.ContadorNotificacoesResponse;
import com.miniCRM.miniCRM.dto.notificacao.NotificacaoResponse;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICACAO_VER')")
    public PageResponse<NotificacaoResponse> listar(
            @RequestParam(name = "lida", required = false) Boolean lida,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @AuthenticationPrincipal Usuario logado) {
        return notificacaoService.listar(logado, lida, page);
    }

    @GetMapping("/contador")
    @PreAuthorize("hasAuthority('NOTIFICACAO_VER')")
    public ContadorNotificacoesResponse contador(@AuthenticationPrincipal Usuario logado) {
        return notificacaoService.contador(logado);
    }

    @PatchMapping("/{id}/lida")
    @PreAuthorize("hasAuthority('NOTIFICACAO_VER')")
    public NotificacaoResponse marcarComoLida(@PathVariable("id") Integer id,
                                              @AuthenticationPrincipal Usuario logado) {
        return notificacaoService.marcarComoLida(id, logado);
    }

    @PatchMapping("/marcar-todas")
    @PreAuthorize("hasAuthority('NOTIFICACAO_VER')")
    public ContadorNotificacoesResponse marcarTodas(@AuthenticationPrincipal Usuario logado) {
        return notificacaoService.marcarTodas(logado);
    }
}
