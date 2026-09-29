package com.miniCRM.miniCRM.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/** RN: text/csv, UTF-8 com BOM (Excel pt-BR), separador ';', CRLF (RFC 4180). */
@Service
public class CsvExportadorService extends AbstractExportadorService<StringBuilder> {

    private static final char BOM = '﻿';
    private static final char SEPARADOR = ';';
    private static final String FIM_DE_LINHA = "\r\n";

    @Override
    public String formato() {
        return "csv";
    }

    @Override
    public MediaType mediaType() {
        return new MediaType("text", "csv", StandardCharsets.UTF_8);
    }

    @Override
    protected StringBuilder abrirDocumento() {
        return new StringBuilder().append(BOM);   // vira EF BB BF ao codificar em UTF-8
    }

    @Override
    protected void escreverCabecalho(StringBuilder saida, List<String> colunas) {
        escreverCampos(saida, colunas);
    }

    @Override
    protected void escreverLinha(StringBuilder saida, List<String> valores) {
        escreverCampos(saida, valores);
    }

    @Override
    protected byte[] fechar(StringBuilder saida) {
        return saida.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void escreverCampos(StringBuilder saida, List<String> campos) {
        saida.append(campos.stream().map(this::escapar)
                        .collect(Collectors.joining(String.valueOf(SEPARADOR))))
                .append(FIM_DE_LINHA);
    }

    /** Campo com separador, aspas ou quebra de linha vai entre aspas, com aspas internas dobradas. */
    private String escapar(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        boolean precisaAspas = valor.indexOf(SEPARADOR) >= 0 || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0;
        return precisaAspas ? '"' + valor.replace("\"", "\"\"") + '"' : valor;
    }
}
