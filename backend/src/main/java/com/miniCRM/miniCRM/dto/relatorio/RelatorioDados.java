package com.miniCRM.miniCRM.dto.relatorio;

import java.util.List;

/** Payload neutro de formato: colunas fixas do RF12 (RN-07) e linhas ja formatadas em texto. */
public record RelatorioDados(String nome, List<String> colunas, List<List<String>> linhas) {
}
