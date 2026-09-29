package com.miniCRM.miniCRM.dto.relatorio;

import org.springframework.http.MediaType;

public record ArquivoExportado(String nomeArquivo, MediaType mediaType, byte[] conteudo) {
}
