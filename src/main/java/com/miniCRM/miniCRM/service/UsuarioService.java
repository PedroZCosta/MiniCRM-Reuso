package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.auth.UsuarioResponse;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> UsuarioResponse.de(usuario))
                .toList();
    }

}