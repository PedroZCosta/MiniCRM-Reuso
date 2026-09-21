package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.auth.UsuarioCriarRequest;
import com.miniCRM.miniCRM.dto.auth.UsuarioCriarResponse;
import com.miniCRM.miniCRM.dto.auth.UsuarioResponse;
import com.miniCRM.miniCRM.dto.auth.UsuarioAtualizarRequest;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    public UsuarioCriarResponse criarUsuario(@Valid @RequestBody UsuarioCriarRequest request,
                                             @AuthenticationPrincipal Usuario logado) {
        return usuarioService.criarUsuario(request, logado);
    }



    @GetMapping()
    @PreAuthorize("hasAuthority('USUARIO_VER')")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }


    @PutMapping("/{id}")
    public UsuarioResponse editarUsuario(@PathVariable Integer id,
                                         @Valid @RequestBody UsuarioAtualizarRequest request) {
        return usuarioService.editarUsuario(id ,request);
    }


    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasAuthority('USUARIO_DESATIVAR')")
    public void desativarUsuario(@PathVariable Integer id,
                                 @AuthenticationPrincipal Usuario logado) {
        usuarioService.desativarUsuario(id, logado);
    }


}

