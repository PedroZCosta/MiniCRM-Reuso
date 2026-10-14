package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteRelatorioLinha;
import com.miniCRM.miniCRM.dto.funil.OportunidadeRelatorioLinha;
import com.miniCRM.miniCRM.dto.relatorio.ArquivoExportado;
import com.miniCRM.miniCRM.dto.relatorio.RelatorioDados;
import com.miniCRM.miniCRM.exception.RequisicaoInvalidaException;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RF12/RF21/RF22: exportação de relatórios. Resolve a Strategy pelo formato pedido e monta o
 * RelatorioDados com as colunas fixas do RF12 (RN-07), já formatadas para o CSV.
 */
@Service
public class RelatorioService {

    private static final String COLUNA_VAZIA = "";
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Map<String, AbstractExportadorService<?>> exportadores;
    private final ClienteService clienteService;
    private final OportunidadeService oportunidadeService;
    private final EscopoCarteira escopoCarteira;

    /** O Spring injeta TODOS os exportadores; o Map é montado aqui (Strategy por formato). */
    public RelatorioService(List<AbstractExportadorService<?>> exportadores,
                            ClienteService clienteService,
                            OportunidadeService oportunidadeService,
                            EscopoCarteira escopoCarteira) {
        this.exportadores = exportadores.stream()
                .collect(Collectors.toMap(e -> e.formato().toLowerCase(Locale.ROOT), Function.identity()));
        this.clienteService = clienteService;
        this.oportunidadeService = oportunidadeService;
        this.escopoCarteira = escopoCarteira;
    }

    /** RN-08/RF22: o escopo é resolvido e aplicado antes da exportação. */
    public ArquivoExportado clientes(String formato, StatusCliente status, LocalDate inicio, LocalDate fim) {
        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);
        List<ClienteRelatorioLinha> linhas =
                clienteService.linhasParaRelatorio(status, inicio, fim, vendedores);

        RelatorioDados dados = new RelatorioDados("relatorio-clientes",
                List.of("Nome", "E-mail", "Telefone", "Empresa", "Status", "Vendedor responsável"),
                linhas.stream().map(l -> List.of(
                        texto(l.nome()), texto(l.email()), texto(l.telefone()),
                        texto(l.empresa()), texto(l.status()), texto(l.vendedorResponsavel()))).toList());

        return exportar(formato, dados);
    }

    public ArquivoExportado oportunidades(String formato, EtapaOportunidade etapa,
                                          LocalDate inicio, LocalDate fim) {
        List<Integer> vendedores = escopoCarteira.vendedoresVisiveis().orElse(null);
        List<OportunidadeRelatorioLinha> linhas =
                oportunidadeService.linhasParaRelatorio(etapa, inicio, fim, vendedores);

        RelatorioDados dados = new RelatorioDados("relatorio-oportunidades",
                List.of("Cliente", "Valor estimado", "Etapa", "Data prevista", "Vendedor responsável"),
                linhas.stream().map(l -> List.of(
                        texto(l.cliente()), moeda(l.valorEstimado()), texto(l.etapa()),
                        data(l.dataPrevista()), texto(l.vendedorResponsavel()))).toList());

        return exportar(formato, dados);
    }

    private ArquivoExportado exportar(String formato, RelatorioDados dados) {
        String chave = formato == null ? "" : formato.trim().toLowerCase(Locale.ROOT);
        AbstractExportadorService<?> exportador = exportadores.get(chave);
        if (exportador == null) {
            throw new RequisicaoInvalidaException("Formato de relatório não suportado: " + formato);
        }

        String nomeArquivo = dados.nome() + "-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "." + exportador.formato();
        return new ArquivoExportado(nomeArquivo, exportador.mediaType(), exportador.exportar(dados));
    }

    // SPEC-00 §2 manda ISO no JSON, mas o CSV é para Excel pt-BR: vírgula decimal e dd/MM/yyyy.
    private String moeda(BigDecimal valor) {
        if (valor == null) {
            return COLUNA_VAZIA;
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));   // não é thread-safe: instância local
        return formato.format(valor);
    }

    private String data(LocalDate data) {
        return data == null ? COLUNA_VAZIA : data.format(DATA_BR);
    }

    private String texto(Object valor) {
        return valor == null ? COLUNA_VAZIA : valor.toString();   // enum -> name()
    }
}
