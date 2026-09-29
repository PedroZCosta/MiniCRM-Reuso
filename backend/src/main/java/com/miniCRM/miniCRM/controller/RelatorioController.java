package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.relatorio.ArquivoExportado;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/clientes")
    @PreAuthorize("hasAuthority('RELATORIO_EXPORTAR')")
    public ResponseEntity<byte[]> clientes(
            @RequestParam(name = "formato", defaultValue = "csv") String formato,
            @RequestParam(name = "status", required = false) StatusCliente status,
            @RequestParam(name = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return resposta(relatorioService.clientes(formato, status, inicio, fim));
    }

    @GetMapping("/oportunidades")
    @PreAuthorize("hasAuthority('RELATORIO_EXPORTAR')")
    public ResponseEntity<byte[]> oportunidades(
            @RequestParam(name = "formato", defaultValue = "csv") String formato,
            @RequestParam(name = "etapa", required = false) EtapaOportunidade etapa,
            @RequestParam(name = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return resposta(relatorioService.oportunidades(formato, etapa, inicio, fim));
    }

    private ResponseEntity<byte[]> resposta(ArquivoExportado arquivo) {
        return ResponseEntity.ok()
                .contentType(arquivo.mediaType())
                .contentLength(arquivo.conteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(arquivo.nomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .body(arquivo.conteudo());
    }
}
