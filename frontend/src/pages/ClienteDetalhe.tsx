import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  type LucideIcon,
  ArrowLeftRight,
  BarChart3,
  Building2,
  CheckSquare,
  ChevronLeft,
  Mail,
  MessageSquare,
  Pencil,
  Phone,
  Plus,
  Trash2,
  User,
  Users,
} from 'lucide-react'
import Avatar from '../components/Avatar'
import ClienteFormulario from '../components/clientes/ClienteFormulario'
import ExcluirCliente from '../components/clientes/ExcluirCliente'
import InteracaoFormulario from '../components/clientes/InteracaoFormulario'
import ItemLinhaDoTempo from '../components/clientes/ItemLinhaDoTempo'
import TransferirCliente from '../components/clientes/TransferirCliente'
import { corDoStatus, rotuloInteracao, rotuloStatus } from '../components/clientes/formato'
import {
  buscarCliente,
  historicoCliente,
  listarInteracoes,
  listarVendedoresAtivos,
} from '../api/clientes'
import { ErroApi, sessao } from '../api/http'
import type {
  ClienteResponse,
  HistoricoItem,
  InteracaoResponse,
  TipoInteracao,
  UsuarioResponse,
} from '../api/tipos'

const iconeDaInteracao: Record<TipoInteracao, LucideIcon> = {
  LIGACAO: Phone,
  EMAIL: Mail,
  REUNIAO: Users,
}

const iconeDoHistorico: Record<HistoricoItem['tipo'], LucideIcon> = {
  INTERACAO: MessageSquare,
  OPORTUNIDADE: BarChart3,
  TAREFA: CheckSquare,
}

type Aba = 'interacoes' | 'historico'

export default function ClienteDetalhe() {
  const { id } = useParams()
  const idCliente = Number(id)
  const logado = sessao.usuario() as UsuarioResponse
  const ehGestor = logado.perfil !== 'VENDEDOR'

  const [cliente, setCliente] = useState<ClienteResponse | null>(null)
  const [interacoes, setInteracoes] = useState<InteracaoResponse[]>([])
  const [historico, setHistorico] = useState<HistoricoItem[]>([])
  const [vendedores, setVendedores] = useState<UsuarioResponse[]>([])
  const [aba, setAba] = useState<Aba>('interacoes')
  const [erro, setErro] = useState('')

  const [editando, setEditando] = useState(false)
  const [registrando, setRegistrando] = useState(false)
  const [transferindo, setTransferindo] = useState(false)
  const [excluindo, setExcluindo] = useState(false)
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setErro('')
    try {
      const [dados, paginaInteracoes, paginaHistorico] = await Promise.all([
        buscarCliente(idCliente),
        listarInteracoes(idCliente),
        historicoCliente(idCliente),
      ])
      setCliente(dados)
      setInteracoes(paginaInteracoes.conteudo)
      setHistorico(paginaHistorico.conteudo)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    }
  }

  useEffect(() => {
    carregar()
    if (ehGestor) listarVendedoresAtivos().then(setVendedores).catch(() => setVendedores([]))
  }, [idCliente])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  function aoSalvar(mensagem: string) {
    setEditando(false)
    setRegistrando(false)
    setTransferindo(false)
    setExcluindo(false)
    setAviso(mensagem)
    carregar()
  }

  const voltar = (
    <Link to="/clientes" className="mb-3 inline-flex items-center gap-1 font-hand text-base text-muted hover:text-ink">
      <ChevronLeft size={16} />
      Clientes
      {cliente && <span className="text-line-soft">/</span>}
      {cliente?.nome}
    </Link>
  )

  if (!cliente) {
    return (
      <>
        {voltar}
        <div className="card py-12 text-center text-muted">{erro || 'Carregando...'}</div>
      </>
    )
  }

  const excluido = cliente.status === 'INATIVO'

  return (
    <>
      {voltar}

      <div className="card mb-[18px] flex flex-wrap items-center gap-4 px-5 py-[18px]">
        <Avatar nome={cliente.nome} />
        <div className="min-w-0 flex-1">
          <h2 className="flex items-center gap-2.5 text-[23px]">
            {cliente.nome}
            <span className={`badge ${corDoStatus[cliente.status]}`}>
              {rotuloStatus[cliente.status]}
            </span>
          </h2>
          <div className="mt-1.5 flex flex-wrap gap-3.5 text-[13px] text-muted">
            {cliente.empresa && (
              <span className="flex items-center gap-1.5">
                <Building2 size={14} />
                {cliente.empresa}
              </span>
            )}
            <span className="flex items-center gap-1.5">
              <Mail size={14} />
              {cliente.email}
            </span>
            {cliente.telefone && (
              <span className="flex items-center gap-1.5">
                <Phone size={14} />
                {cliente.telefone}
              </span>
            )}
            <span className="flex items-center gap-1.5">
              <User size={14} />
              {cliente.vendedor.nome}
              {!cliente.vendedor.ativo && <b className="text-red">(sem responsável ativo)</b>}
            </span>
          </div>
        </div>

        {!excluido && (
          <div className="flex flex-wrap gap-2">
            <button className="btn" onClick={() => setEditando(true)}>
              <Pencil size={15} />
              Editar
            </button>
            {ehGestor && (
              <button className="btn" onClick={() => setTransferindo(true)}>
                <ArrowLeftRight size={15} />
                Transferir
              </button>
            )}
            <button className="btn btn-danger" onClick={() => setExcluindo(true)}>
              <Trash2 size={15} />
              Excluir
            </button>
            <button className="btn btn-pri" onClick={() => setRegistrando(true)}>
              <Plus size={15} />
              Registrar interação
            </button>
          </div>
        )}
      </div>

      {excluido && (
        <div className="modal-note mb-4">
          Cliente excluído. Ele não recebe novas interações, mas o histórico continua disponível.
        </div>
      )}

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      <div className="tabs">
        <button className={`tab-btn ${aba === 'interacoes' ? 'active' : ''}`} onClick={() => setAba('interacoes')}>
          Interações <span className="n">({interacoes.length})</span>
        </button>
        <button className={`tab-btn ${aba === 'historico' ? 'active' : ''}`} onClick={() => setAba('historico')}>
          Histórico <span className="n">({historico.length})</span>
        </button>
      </div>

      <div className="card px-[18px] py-1">
        {aba === 'interacoes' && interacoes.length === 0 && (
          <div className="py-10 text-center text-muted">Nenhuma interação registrada</div>
        )}

        {aba === 'interacoes' &&
          interacoes.map((interacao) => (
            <ItemLinhaDoTempo
              key={interacao.idInteracao}
              Icone={iconeDaInteracao[interacao.tipo]}
              titulo={rotuloInteracao[interacao.tipo]}
              data={interacao.dataInteracao}
              texto={interacao.observacoes}
              autor={interacao.autor}
            />
          ))}

        {aba === 'historico' && historico.length === 0 && (
          <div className="py-10 text-center text-muted">Nada no histórico ainda</div>
        )}

        {aba === 'historico' &&
          historico.map((item, i) => (
            <ItemLinhaDoTempo
              key={i}
              Icone={iconeDoHistorico[item.tipo]}
              // nas interacoes o titulo vem como LIGACAO, EMAIL ou REUNIAO.
              titulo={rotuloInteracao[item.titulo as TipoInteracao] ?? item.titulo}
              data={item.data}
              texto={item.descricao}
              autor={item.autor}
            />
          ))}
      </div>

      {editando && (
        <ClienteFormulario
          cliente={cliente}
          vendedores={[]}
          aoFechar={() => setEditando(false)}
          aoSalvar={() => aoSalvar('Alterações salvas')}
        />
      )}

      {registrando && (
        <InteracaoFormulario
          cliente={cliente}
          aoFechar={() => setRegistrando(false)}
          aoSalvar={() => aoSalvar('Interação registrada')}
        />
      )}

      {transferindo && (
        <TransferirCliente
          cliente={cliente}
          vendedores={vendedores}
          aoFechar={() => setTransferindo(false)}
          aoSalvar={() => aoSalvar('Cliente transferido')}
        />
      )}

      {excluindo && (
        <ExcluirCliente
          cliente={cliente}
          aoFechar={() => setExcluindo(false)}
          aoExcluir={() => aoSalvar('Cliente excluído')}
        />
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
