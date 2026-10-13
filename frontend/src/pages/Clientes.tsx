import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { ChevronLeft, ChevronRight, Pencil, Plus, Search, Trash2 } from 'lucide-react'
import Avatar from '../components/Avatar'
import ClienteFormulario from '../components/clientes/ClienteFormulario'
import ExcluirCliente from '../components/clientes/ExcluirCliente'
import { corDoStatus, rotuloStatus } from '../components/clientes/formato'
import { listarClientes, listarVendedoresAtivos } from '../api/clientes'
import { ErroApi, sessao } from '../api/http'
import type { ClienteResponse, StatusCliente, UsuarioResponse } from '../api/tipos'

const filtrosDeStatus: { valor: StatusCliente | ''; rotulo: string }[] = [
  { valor: '', rotulo: 'Todos' },
  { valor: 'PROSPECT', rotulo: 'Prospect' },
  { valor: 'ATIVO', rotulo: 'Ativo' },
  { valor: 'INATIVO', rotulo: 'Inativo' },
]

export default function Clientes() {
  const navegar = useNavigate()
  const logado = sessao.usuario() as UsuarioResponse
  const ehGestor = logado.perfil !== 'VENDEDOR'

  const [clientes, setClientes] = useState<ClienteResponse[]>([])
  const [total, setTotal] = useState(0)
  const [totalPaginas, setTotalPaginas] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  const [busca, setBusca] = useState('')
  const [status, setStatus] = useState<StatusCliente | ''>('')
  const [vendedorId, setVendedorId] = useState('')
  const [incluirExcluidos, setIncluirExcluidos] = useState(false)
  const [pagina, setPagina] = useState(0)

  const [vendedores, setVendedores] = useState<UsuarioResponse[]>([])
  const [criando, setCriando] = useState(false)
  const [editando, setEditando] = useState<ClienteResponse | null>(null)
  const [excluindo, setExcluindo] = useState<ClienteResponse | null>(null)
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setCarregando(true)
    setErro('')
    try {
      const resposta = await listarClientes({ busca, status, vendedorId, incluirExcluidos, pagina })
      setClientes(resposta.conteudo)
      setTotal(resposta.totalRegistros)
      setTotalPaginas(resposta.totalPaginas)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    } finally {
      setCarregando(false)
    }
  }

  // espera a pessoa parar de digitar antes de buscar no servidor.
  useEffect(() => {
    const relogio = setTimeout(carregar, 300)
    return () => clearTimeout(relogio)
  }, [busca, status, vendedorId, incluirExcluidos, pagina])

  useEffect(() => {
    if (ehGestor) listarVendedoresAtivos().then(setVendedores).catch(() => setVendedores([]))
  }, [])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  // qualquer filtro novo volta para a primeira pagina.
  function filtrar(mudar: () => void) {
    mudar()
    setPagina(0)
  }

  function aoSalvar(mensagem: string) {
    setCriando(false)
    setEditando(null)
    setExcluindo(null)
    setAviso(mensagem)
    carregar()
  }

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <div className="relative max-w-[300px] min-w-[220px] flex-1">
          <Search size={15} className="absolute top-1/2 left-3 -translate-y-1/2 text-muted" />
          <input
            className="input h-[39px] w-full pl-9"
            placeholder="Buscar por nome ou empresa"
            value={busca}
            onChange={(e) => filtrar(() => setBusca(e.target.value))}
          />
        </div>

        <div className="flex flex-wrap gap-2">
          {filtrosDeStatus.map((f) => (
            <button
              key={f.rotulo}
              className={`chip ${status === f.valor ? 'on' : ''}`}
              onClick={() => filtrar(() => setStatus(f.valor))}
            >
              {f.rotulo}
            </button>
          ))}
        </div>

        {ehGestor && (
          <>
            <select
              className="select"
              value={vendedorId}
              onChange={(e) => filtrar(() => setVendedorId(e.target.value))}
            >
              <option value="">Todos os vendedores</option>
              {vendedores.map((v) => (
                <option key={v.idUsuario} value={v.idUsuario}>
                  {v.nome}
                </option>
              ))}
            </select>

            <label className="flex items-center gap-1.5 font-hand text-[15px] text-ink-soft">
              <input
                type="checkbox"
                checked={incluirExcluidos}
                onChange={(e) => filtrar(() => setIncluirExcluidos(e.target.checked))}
              />
              mostrar excluídos
            </label>
          </>
        )}

        <div className="flex-1" />

        <button className="btn btn-pri" onClick={() => setCriando(true)}>
          <Plus size={16} />
          Novo cliente
        </button>
      </div>

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      <div className="card overflow-hidden">
        <table className="dt">
          <thead>
            <tr>
              <th>Cliente</th>
              <th>Empresa</th>
              <th>Vendedor</th>
              <th>Status</th>
              <th className="text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {carregando && clientes.length === 0 && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted">
                  Carregando...
                </td>
              </tr>
            )}

            {!carregando && clientes.length === 0 && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted">
                  Nenhum cliente encontrado
                </td>
              </tr>
            )}

            {clientes.map((cliente) => (
              <tr
                key={cliente.idCliente}
                className="cursor-pointer"
                onClick={() => navegar(`/clientes/${cliente.idCliente}`)}
              >
                <td>
                  <div className="flex items-center gap-2.5">
                    <Avatar nome={cliente.nome} />
                    <div className="min-w-0">
                      <div className="font-semibold">{cliente.nome}</div>
                      <div className="text-xs text-muted">{cliente.email}</div>
                    </div>
                  </div>
                </td>
                <td className="text-ink-soft">{cliente.empresa || '—'}</td>
                <td className="text-ink-soft">
                  {cliente.vendedor.nome}
                  {!cliente.vendedor.ativo && (
                    <div className="text-xs text-red">sem responsável ativo</div>
                  )}
                </td>
                <td>
                  <span className={`badge ${corDoStatus[cliente.status]}`}>
                    {rotuloStatus[cliente.status]}
                  </span>
                </td>
                <td>
                  {/* o clique nos botoes nao pode abrir a ficha do cliente. */}
                  <div className="flex justify-end gap-1" onClick={(e) => e.stopPropagation()}>
                    {cliente.status !== 'INATIVO' && (
                      <>
                        <button className="btn btn-sm" onClick={() => setEditando(cliente)}>
                          <Pencil size={14} />
                          Editar
                        </button>
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => setExcluindo(cliente)}
                          title="Excluir"
                        >
                          <Trash2 size={14} />
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex items-center justify-between border-t-2 border-line px-4 py-2.5 text-xs text-muted">
          <span>
            {total} {total === 1 ? 'cliente' : 'clientes'}
            {logado.perfil === 'VENDEDOR' && ' (apenas sua carteira)'}
          </span>

          {totalPaginas > 1 && (
            <div className="pager">
              <button disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>
                <ChevronLeft size={14} />
              </button>
              {Array.from({ length: totalPaginas }, (_, i) => (
                <button key={i} className={pagina === i ? 'on' : ''} onClick={() => setPagina(i)}>
                  {i + 1}
                </button>
              ))}
              <button disabled={pagina >= totalPaginas - 1} onClick={() => setPagina(pagina + 1)}>
                <ChevronRight size={14} />
              </button>
            </div>
          )}
        </div>
      </div>

      {criando && (
        <ClienteFormulario
          vendedores={vendedores}
          aoFechar={() => setCriando(false)}
          aoSalvar={() => aoSalvar('Cliente cadastrado')}
        />
      )}

      {editando && (
        <ClienteFormulario
          cliente={editando}
          vendedores={vendedores}
          aoFechar={() => setEditando(null)}
          aoSalvar={() => aoSalvar('Alterações salvas')}
        />
      )}

      {excluindo && (
        <ExcluirCliente
          cliente={excluindo}
          aoFechar={() => setExcluindo(null)}
          aoExcluir={() => aoSalvar('Cliente excluído')}
        />
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
