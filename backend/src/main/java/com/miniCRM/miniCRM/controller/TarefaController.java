package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.tarefa.CriarTarefaRequest;
import com.miniCRM.miniCRM.dto.tarefa.EditarTarefaRequest;
import com.miniCRM.miniCRM.dto.tarefa.TarefaResponse;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.FaixaTarefa;
import com.miniCRM.miniCRM.model.enums.SituacaoTarefa;
import com.miniCRM.miniCRM.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping
    @PreAuthorize("hasAuthority('TAREFA_CRIAR')")
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody CriarTarefaRequest request,
                                                @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaService.criar(request, logado));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    public PageResponse<TarefaResponse> listar(
            @RequestParam(name = "situacao", required = false) SituacaoTarefa situacao,
            @RequestParam(name = "faixa", required = false) FaixaTarefa faixa,
            @RequestParam(name = "clienteId", required = false) Integer clienteId,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        return tarefaService.listar(situacao, faixa, clienteId, page);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    public TarefaResponse editar(@PathVariable("id") Integer id,
                                 @Valid @RequestBody EditarTarefaRequest request) {
        return tarefaService.editar(id, request);
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasAuthority('TAREFA_CONCLUIR')")
    public TarefaResponse concluir(@PathVariable("id") Integer id) {
        return tarefaService.concluir(id);
    }

    @PatchMapping("/{id}/reabrir")
    @PreAuthorize("hasAuthority('TAREFA_CONCLUIR')")
    public TarefaResponse reabrir(@PathVariable("id") Integer id) {
        return tarefaService.reabrir(id);
    }
}
