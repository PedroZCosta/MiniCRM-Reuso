package com.miniCRM.miniCRM.service;

import com.miniCRM.miniCRM.dto.cliente.ClienteResumo;
import com.miniCRM.miniCRM.dto.cliente.CriarClienteRequest;
import com.miniCRM.miniCRM.dto.cliente.EditarClienteRequest;
import com.miniCRM.miniCRM.exception.RecursoNaoEncontradoException;
import com.miniCRM.miniCRM.exception.RegraNegocioException;
import com.miniCRM.miniCRM.exception.SemPermissaoException;
import com.miniCRM.miniCRM.model.Cliente;
import com.miniCRM.miniCRM.model.Usuario;
import com.miniCRM.miniCRM.model.enums.PerfilUsuario;
import com.miniCRM.miniCRM.model.enums.StatusCliente;
import com.miniCRM.miniCRM.repository.ClienteRepository;
import com.miniCRM.miniCRM.repository.ClienteSpecs;
import com.miniCRM.miniCRM.repository.UsuarioRepository;
import com.miniCRM.miniCRM.security.EscopoCarteira;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Singleton nº 2 do trabalho (SPEC-02 §4): instância única gerenciada pelo Spring.
 */
@Service
@RequiredArgsConstructor
public class ClienteService implements ClienteConsultaService {

    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final int TAMANHO_PAGINA = 20;

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscopoCarteira escopoCarteira;

    public Cliente criar(CriarClienteRequest dto, Integer idUsuarioLogado) {
        validarNomeEmail(dto.nome(), dto.email());

        PerfilUsuario perfilLogado = buscarUsuarioLogado(idUsuarioLogado).getPerfil();
        Integer idVendedor = perfilLogado == PerfilUsuario.VENDEDOR
                ? idUsuarioLogado
                : dto.idVendedor() != null ? dto.idVendedor() : idUsuarioLogado;
        Usuario vendedor = buscarVendedorAtivo(idVendedor);

        Cliente cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setEmpresa(dto.empresa());
        cliente.setStatus(StatusCliente.PROSPECT);
        cliente.setVendedor(vendedor);
        cliente.setExcluido(false);
        return clienteRepository.save(cliente);
    }

    public Page<Cliente> listar(String busca, StatusCliente status, Integer vendedorId,
                                 boolean incluirExcluidos, int page, Integer idUsuarioLogado) {
        PerfilUsuario perfilLogado = buscarUsuarioLogado(idUsuarioLogado).getPerfil();
        boolean podeVerExcluidos = incluirExcluidos
                && (perfilLogado == PerfilUsuario.GERENTE || perfilLogado == PerfilUsuario.ADMIN);

        Specification<Cliente> spec = podeVerExcluidos
                ? Specification.where((Specification<Cliente>) null)
                : ClienteSpecs.naoExcluido();
        spec = spec
                .and(busca != null && !busca.isBlank() ? ClienteSpecs.buscaLivre(busca) : null)
                .and(status != null ? ClienteSpecs.comStatus(status) : null)
                .and(vendedorId != null ? ClienteSpecs.doVendedor(vendedorId) : null)
                .and(escopoCarteira.vendedoresVisiveis().map(ClienteSpecs::dentroDoEscopo).orElse(null));

        int paginaSegura = Math.max(page, 0);
        return clienteRepository.findAll(spec, PageRequest.of(paginaSegura, TAMANHO_PAGINA, Sort.by("nome")));
    }

    public Cliente buscarPorId(Integer idCliente) {
        return buscarNoEscopo(idCliente);
    }

    public Cliente editar(Integer idCliente, EditarClienteRequest dto) {
        Cliente cliente = buscarNoEscopo(idCliente);
        validarNomeEmail(dto.nome(), dto.email());

        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setEmpresa(dto.empresa());
        return clienteRepository.save(cliente);
    }

    public void excluir(Integer idCliente) {
        Cliente cliente = buscarNoEscopo(idCliente);
        cliente.setExcluido(true);
        cliente.setStatus(StatusCliente.INATIVO);
        clienteRepository.save(cliente);
    }

    public Cliente transferir(Integer idCliente, Integer novoVendedorId, Integer idUsuarioLogado) {
        PerfilUsuario perfilLogado = buscarUsuarioLogado(idUsuarioLogado).getPerfil();
        if (perfilLogado == PerfilUsuario.VENDEDOR) {
            throw new SemPermissaoException("Vendedor não pode transferir carteira");
        }

        Cliente cliente = buscarNoEscopo(idCliente);
        Usuario novoVendedor = buscarVendedorAtivo(novoVendedorId);
        cliente.setVendedor(novoVendedor);
        return clienteRepository.save(cliente);
    }

    /** Usado pelo listener do RF30 (SPEC-02 §5). PROSPECT -> ATIVO, uma única vez. */
    public void promoverParaAtivoSeProspect(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        if (cliente.getStatus() == StatusCliente.PROSPECT) {
            cliente.setStatus(StatusCliente.ATIVO);
            clienteRepository.save(cliente);
        }
    }

    /** Contrato público (ClienteConsultaService) consumido pela SPEC-03/04. */
    @Override
    public ClienteResumo buscarAtivo(Integer idCliente) {
        Cliente cliente = buscarClienteAtivo(idCliente);
        return new ClienteResumo(cliente.getIdCliente(), cliente.getNome(),
                cliente.getEmail(), cliente.getEmpresa(), cliente.getStatus());
    }

    /**
     * Uso interno do módulo (ex.: SPEC-03 ao vincular a entidade na oportunidade).
     * 404 se não existir; 422 se excluido=true (não se cria nada para cliente excluído).
     */
    public Cliente buscarClienteAtivo(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        if (cliente.getExcluido()) {
            throw new RegraNegocioException("Cliente excluído");
        }
        return cliente;
    }

    private Cliente buscarNoEscopo(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));

        List<Integer> visiveis = escopoCarteira.vendedoresVisiveis().orElse(null);
        if (visiveis != null && !visiveis.contains(cliente.getVendedor().getIdUsuario())) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado");
        }
        return cliente;
    }

    private Usuario buscarUsuarioLogado(Integer idUsuarioLogado) {
        return usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private Usuario buscarVendedorAtivo(Integer idVendedor) {
        Usuario vendedor = usuarioRepository.findById(idVendedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado"));
        if (!vendedor.getAtivo()) {
            throw new RegraNegocioException("Vendedor responsável precisa estar ativo");
        }
        return vendedor;
    }

    private void validarNomeEmail(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Nome é obrigatório");
        }
        if (email == null || !EMAIL_VALIDO.matcher(email).matches()) {
            throw new RegraNegocioException("E-mail obrigatório e deve ter formato válido");
        }
    }
}
