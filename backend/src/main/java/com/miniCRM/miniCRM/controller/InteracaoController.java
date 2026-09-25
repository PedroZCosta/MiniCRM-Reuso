package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.cliente.CriarInteracaoRequest;
import com.miniCRM.miniCRM.dto.cliente.InteracaoResponse;
import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.model.Interacao;
import com.miniCRM.miniCRM.service.InteracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes/{idCliente}/interacoes")
@RequiredArgsConstructor
public class InteracaoController {

    private final InteracaoService interacaoService;

    @PostMapping
    @PreAuthorize("hasAuthority('INTERACAO_CRIAR')")
    public ResponseEntity<InteracaoResponse> criar(
            @PathVariable Integer idCliente, @RequestBody CriarInteracaoRequest request) {
        Interacao interacao = interacaoService.criar(idCliente, request, usuarioLogadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResponse(interacao));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INTERACAO_VER')")
    public PageResponse<InteracaoResponse> listar(
            @PathVariable Integer idCliente, @RequestParam(defaultValue = "0") int page) {
        Page<Interacao> pagina = interacaoService.listar(idCliente, page);
        return new PageResponse<>(
                pagina.getContent().stream().map(this::paraResponse).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalPages(), pagina.getTotalElements());
    }

    private Integer usuarioLogadoId() {
        return Integer.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private InteracaoResponse paraResponse(Interacao interacao) {
        return new InteracaoResponse(
                interacao.getIdInteracao(), interacao.getTipo(), interacao.getDataInteracao(),
                interacao.getObservacoes(), interacao.getUsuario().getNome(), interacao.getCriadoEm());
    }
}
