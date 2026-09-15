package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.dto.funil.CriarOportunidadeRequest;
import com.miniCRM.miniCRM.dto.funil.EditarOportunidadeRequest;
import com.miniCRM.miniCRM.dto.funil.FunilResponse;
import com.miniCRM.miniCRM.dto.funil.MotivoPerdaResponse;
import com.miniCRM.miniCRM.dto.funil.MoverEtapaRequest;
import com.miniCRM.miniCRM.dto.funil.OportunidadeDetalheResponse;
import com.miniCRM.miniCRM.dto.funil.OportunidadeResponse;
import com.miniCRM.miniCRM.model.enums.EtapaOportunidade;
import com.miniCRM.miniCRM.service.OportunidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OportunidadeController {

    private final OportunidadeService oportunidadeService;

    @PostMapping("/oportunidades")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_CRIAR')")
    public ResponseEntity<OportunidadeResponse> criar(@RequestBody CriarOportunidadeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oportunidadeService.criar(request));
    }

    @GetMapping("/oportunidades")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_VER')")
    public PageResponse<OportunidadeResponse> listar(
            @RequestParam(name = "etapa", required = false) EtapaOportunidade etapa,
            @RequestParam(name = "clienteId", required = false) Integer clienteId,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        return oportunidadeService.listar(etapa, clienteId, page);
    }

    @GetMapping("/oportunidades/{id}")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_VER')")
    public OportunidadeDetalheResponse buscarPorId(@PathVariable("id") Integer id) {
        return oportunidadeService.buscarPorId(id);
    }

    @PutMapping("/oportunidades/{id}")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_EDITAR')")
    public OportunidadeResponse editar(@PathVariable("id") Integer id,
                                       @RequestBody EditarOportunidadeRequest request) {
        return oportunidadeService.editar(id, request);
    }

    @PatchMapping("/oportunidades/{id}/etapa")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_MOVER')")
    public OportunidadeResponse moverEtapa(@PathVariable("id") Integer id,
                                           @RequestBody MoverEtapaRequest request) {
        return oportunidadeService.moverEtapa(id, request);
    }

    @PatchMapping("/oportunidades/{id}/reabrir")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_REABRIR')")
    public OportunidadeResponse reabrir(@PathVariable("id") Integer id) {
        return oportunidadeService.reabrir(id);
    }

    @GetMapping("/funil")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_VER')")
    public FunilResponse funil() {
        return oportunidadeService.montarFunil();
    }

    @GetMapping("/motivos-perda")
    @PreAuthorize("hasAuthority('OPORTUNIDADE_VER')")
    public List<MotivoPerdaResponse> motivosPerda() {
        return oportunidadeService.listarMotivosPerda();
    }
}
