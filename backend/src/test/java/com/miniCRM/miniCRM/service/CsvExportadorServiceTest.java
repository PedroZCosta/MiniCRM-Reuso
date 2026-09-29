package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.relatorio.RelatorioDados;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportadorServiceTest {

    private final CsvExportadorService exportador = new CsvExportadorService();

    private String textoSemBom(byte[] conteudo) {
        String texto = new String(conteudo, StandardCharsets.UTF_8);
        return texto.startsWith("﻿") ? texto.substring(1) : texto;
    }

    @Test
    @DisplayName("campo com ponto e virgula sai entre aspas")
    void campoComPontoEVirgulaSaiEntreAspas() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col"),
                List.of(List.of("valor;com;separador")));

        String texto = textoSemBom(exportador.exportar(dados));

        assertThat(texto).contains("\"valor;com;separador\"");
    }

    @Test
    @DisplayName("aspas internas sao dobradas")
    void aspasInternasSaoDobradas() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col"),
                List.of(List.of("disse \"ola\"")));

        String texto = textoSemBom(exportador.exportar(dados));

        assertThat(texto).contains("\"disse \"\"ola\"\"\"");
    }

    @Test
    @DisplayName("quebra de linha no campo sai entre aspas")
    void quebraDeLinhaNoCampoSaiEntreAspas() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col"),
                List.of(List.of("linha1\nlinha2")));

        String texto = textoSemBom(exportador.exportar(dados));

        assertThat(texto).contains("\"linha1\nlinha2\"");
    }

    @Test
    @DisplayName("campo nulo sai vazio")
    void campoNuloSaiVazio() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col1", "Col2"),
                List.of(Arrays.asList("valor", null)));

        String texto = textoSemBom(exportador.exportar(dados));
        String[] linhas = texto.split("\r\n");

        assertThat(linhas[1]).isEqualTo("valor;");
    }

    @Test
    @DisplayName("linhas terminam com crlf")
    void linhasTerminamComCrLf() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col"), List.of(List.of("a"), List.of("b")));

        byte[] conteudo = exportador.exportar(dados);
        String texto = new String(conteudo, StandardCharsets.UTF_8);

        assertThat(texto).contains("Col\r\na\r\nb\r\n");
    }

    @Test
    @DisplayName("documento comeca com o bom utf-8")
    void documentoComecaComOBom() {
        RelatorioDados dados = new RelatorioDados("teste", List.of("Col"), List.of());

        byte[] conteudo = exportador.exportar(dados);

        assertThat(conteudo[0] & 0xFF).isEqualTo(0xEF);
        assertThat(conteudo[1] & 0xFF).isEqualTo(0xBB);
        assertThat(conteudo[2] & 0xFF).isEqualTo(0xBF);
    }
}
