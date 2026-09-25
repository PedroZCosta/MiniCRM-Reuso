package com.miniCRM.miniCRM.controller;

import com.miniCRM.miniCRM.dto.cliente.ClienteResponse;
import com.miniCRM.miniCRM.dto.cliente.CriarClienteRequest;
import com.miniCRM.miniCRM.dto.cliente.EditarClienteRequest;
import com.miniCRM.miniCRM.dto.cliente.TransferirClienteRequest;
import com.miniCRM.miniCRM.dto.cliente.VendedorResumo;
import com.miniCRM.miniCRM.dto.comum.HistoricoItem;
import com.miniCRM.miniCRM.dto.comum.PageResponse;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.service.ClienteService;
import com.miniCRM.miniCRM.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final HistoricoService historicoService;

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE_VER')")
    public PageResponse<ClienteResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusCliente status,
            @RequestParam(required = false) Integer vendedorId,
            @RequestParam(defaultValue = "false") boolean incluirExcluidos,
            @RequestParam(defaultValue = "0") int page) {
        Page<Cliente> pagina = clienteService.listar(
                busca, status, vendedorId, incluirExcluidos, page, usuarioLogadoId());
        return paraPageResponse(pagina);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE_CRIAR')")
    public ResponseEntity<ClienteResponse> criar(@RequestBody CriarClienteRequest request) {
        Cliente cliente = clienteService.criar(request, usuarioLogadoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResponse(cliente));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_VER')")
    public ClienteResponse buscarPorId(@PathVariable Integer id) {
        return paraResponse(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_EDITAR')")
    public ClienteResponse editar(@PathVariable Integer id, @RequestBody EditarClienteRequest request) {
        return paraResponse(clienteService.editar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/transferir")
    @PreAuthorize("hasAuthority('CLIENTE_TRANSFERIR')")
    public ClienteResponse transferir(@PathVariable Integer id, @RequestBody TransferirClienteRequest request) {
        Cliente cliente = clienteService.transferir(id, request.novoVendedorId(), usuarioLogadoId());
        return paraResponse(cliente);
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAuthority('CLIENTE_VER')")
    public PageResponse<HistoricoItem> historico(
            @PathVariable Integer id, @RequestParam(defaultValue = "0") int page) {
        return historicoService.listar(id, page);
    }

    private Integer usuarioLogadoId() {
        return Integer.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private PageResponse<ClienteResponse> paraPageResponse(Page<Cliente> pagina) {
        return new PageResponse<>(
                pagina.getContent().stream().map(this::paraResponse).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalPages(), pagina.getTotalElements());
    }

    private ClienteResponse paraResponse(Cliente cliente) {
        Usuario vendedor = cliente.getVendedor();
        return new ClienteResponse(
                cliente.getIdCliente(), cliente.getNome(), cliente.getEmail(),
                cliente.getTelefone(), cliente.getEmpresa(), cliente.getStatus(),
                new VendedorResumo(vendedor.getIdUsuario(), vendedor.getNome(), vendedor.getAtivo()),
                cliente.getCriadoEm());
    }
}
