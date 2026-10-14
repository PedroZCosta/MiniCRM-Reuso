import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { ChevronLeft, ChevronRight, Check, Pencil, Plus, RotateCcw } from 'lucide-react'
import TarefaFormulario from '../components/tarefas/TarefaFormulario'
import { corDaSituacao, formatarData, rotuloSituacao } from '../components/tarefas/formato'
import { listarVendedoresAtivos } from '../api/clientes'
import { ErroApi, sessao } from '../api/http'
import { concluirTarefa, listarTarefas, reabrirTarefa } from '../api/tarefas'
import type { FaixaTarefa, SituacaoTarefa, TarefaResponse, UsuarioResponse } from '../api/tipos'

// cada chip vira situacao OU faixa na chamada: o backend nao aceita os dois juntos.
type Chave = '' | SituacaoTarefa | Exclude<FaixaTarefa, 'VENCIDA'>

const filtros: { chave: Chave; rotulo: string }[] = [
  { chave: '', rotulo: 'Todas' },
  { chave: 'PENDENTE', rotulo: 'Pendentes' },
  { chave: 'VENCIDA', rotulo: 'Vencidas' },
  { chave: 'CONCLUIDA', rotulo: 'Concluídas' },
  { chave: 'HOJE', rotulo: 'Vencem hoje' },
  { chave: 'D1', rotulo: 'Vencem amanhã' },
  { chave: 'D15', rotulo: 'Próximos 15 dias' },
]

const situacoes: Chave[] = ['PENDENTE', 'VENCIDA', 'CONCLUIDA']

export default function Tarefas() {
  const logado = sessao.usuario() as UsuarioResponse
  const ehGestor = logado.perfil !== 'VENDEDOR'

  const [tarefas, setTarefas] = useState<TarefaResponse[]>([])
  const [total, setTotal] = useState(0)
  const [totalPaginas, setTotalPaginas] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  const [filtro, setFiltro] = useState<Chave>('')
  const [pagina, setPagina] = useState(0)

  const [responsaveis, setResponsaveis] = useState<UsuarioResponse[]>([])
  const [criando, setCriando] = useState(false)
  const [editando, setEditando] = useState<TarefaResponse | null>(null)
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setCarregando(true)
    setErro('')
    try {
      const resposta = await listarTarefas({
        situacao: situacoes.includes(filtro) ? (filtro as SituacaoTarefa) : '',
        faixa: filtro && !situacoes.includes(filtro) ? (filtro as FaixaTarefa) : '',
        pagina,
      })
      setTarefas(resposta.conteudo)
      setTotal(resposta.totalRegistros)
      setTotalPaginas(resposta.totalPaginas)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    carregar()
  }, [filtro, pagina])

  useEffect(() => {
    if (ehGestor) listarVendedoresAtivos().then(setResponsaveis).catch(() => setResponsaveis([]))
  }, [])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  // qualquer filtro novo volta para a primeira pagina.
  function filtrar(chave: Chave) {
    setFiltro(chave)
    setPagina(0)
  }

  function aoSalvar(mensagem: string) {
    setCriando(false)
    setEditando(null)
    setAviso(mensagem)
    carregar()
  }

  async function alternarConclusao(tarefa: TarefaResponse) {
    setErro('')
    try {
      if (tarefa.concluida) {
        await reabrirTarefa(tarefa.idTarefa)
        setAviso('Tarefa reaberta')
      } else {
        await concluirTarefa(tarefa.idTarefa)
        setAviso('Tarefa concluída')
      }
      carregar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível atualizar a tarefa')
    }
  }

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <div className="flex flex-wrap gap-2">
          {filtros.map((f) => (
            <button
              key={f.rotulo}
              className={`chip ${filtro === f.chave ? 'on' : ''}`}
              onClick={() => filtrar(f.chave)}
            >
              {f.rotulo}
            </button>
          ))}
        </div>

        <div className="flex-1" />

        <button className="btn btn-pri" onClick={() => setCriando(true)}>
          <Plus size={16} />
          Nova tarefa
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
              <th>Tarefa</th>
              <th>Cliente</th>
              <th>Responsável</th>
              <th>Vencimento</th>
              <th>Situação</th>
              <th className="text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {carregando && tarefas.length === 0 && (
              <tr>
                <td colSpan={6} className="py-8 text-center text-muted">
                  Carregando...
                </td>
              </tr>
            )}

            {!carregando && tarefas.length === 0 && (
              <tr>
                <td colSpan={6} className="py-8 text-center text-muted">
                  Nenhuma tarefa encontrada
                </td>
              </tr>
            )}

            {tarefas.map((tarefa) => (
              <tr key={tarefa.idTarefa}>
                <td>
                  <div className={`font-semibold ${tarefa.concluida ? 'text-muted line-through' : ''}`}>
                    {tarefa.titulo}
                  </div>
                  {tarefa.descricao && (
                    <div className="max-w-[320px] truncate text-xs text-muted">{tarefa.descricao}</div>
                  )}
                </td>
                <td className="text-ink-soft">
                  {tarefa.idCliente ? (
                    <Link to={`/clientes/${tarefa.idCliente}`} className="hover:underline">
                      {tarefa.cliente}
                    </Link>
                  ) : (
                    '—'
                  )}
                </td>
                <td className="text-ink-soft">{tarefa.responsavel}</td>
                <td className="text-ink-soft">{formatarData(tarefa.dataVencimento)}</td>
                <td>
                  <span className={`badge ${corDaSituacao[tarefa.situacao]}`}>
                    {rotuloSituacao[tarefa.situacao]}
                  </span>
                </td>
                <td>
                  <div className="flex justify-end gap-1">
                    {!tarefa.concluida && (
                      <button className="btn btn-sm" onClick={() => setEditando(tarefa)}>
                        <Pencil size={14} />
                        Editar
                      </button>
                    )}
                    <button className="btn btn-sm" onClick={() => alternarConclusao(tarefa)}>
                      {tarefa.concluida ? <RotateCcw size={14} /> : <Check size={14} />}
                      {tarefa.concluida ? 'Reabrir' : 'Concluir'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex items-center justify-between border-t-2 border-line px-4 py-2.5 text-xs text-muted">
          <span>
            {total} {total === 1 ? 'tarefa' : 'tarefas'}
            {logado.perfil === 'VENDEDOR' && ' (apenas suas tarefas)'}
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
        <TarefaFormulario
          responsaveis={responsaveis}
          aoFechar={() => setCriando(false)}
          aoSalvar={() => aoSalvar('Tarefa criada')}
        />
      )}

      {editando && (
        <TarefaFormulario
          tarefa={editando}
          responsaveis={responsaveis}
          aoFechar={() => setEditando(null)}
          aoSalvar={() => aoSalvar('Alterações salvas')}
        />
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
