package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.relatorio.RelatorioDados;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * Strategy + Template Method (SPEC-04 §5), no mesmo estilo de config/SeedBase. Cada formato é uma
 * implementação intercambiável escolhida por {@link #formato()}, e {@link #exportar} é o algoritmo
 * fixo. {@code D} é o documento em construção (StringBuilder no CSV, ByteArrayOutputStream num PDF).
 */
public abstract class AbstractExportadorService<D> {

    /** O template: fixo, final. */
    public final byte[] exportar(RelatorioDados dados) {
        D saida = abrirDocumento();
        escreverCabecalho(saida, dados.colunas());
        dados.linhas().forEach(linha -> escreverLinha(saida, linha));
        return fechar(saida);
    }

    /** Chave da Strategy: "csv" hoje, "pdf" depois, sem tocar no RelatorioService. */
    public abstract String formato();

    public abstract MediaType mediaType();

    protected abstract D abrirDocumento();

    protected abstract void escreverCabecalho(D saida, List<String> colunas);

    protected abstract void escreverLinha(D saida, List<String> valores);

    protected abstract byte[] fechar(D saida);
}
