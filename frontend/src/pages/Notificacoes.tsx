import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { Bell, CheckCheck, ChevronLeft, ChevronRight } from 'lucide-react'
import { formatarDataHora } from '../components/clientes/formato'
import { ErroApi } from '../api/http'
import { listarNotificacoes, marcarComoLida, marcarTodasComoLidas } from '../api/notificacoes'
import type { NotificacaoResponse } from '../api/tipos'

// o menu lateral escuta este evento para atualizar o numero do sino.
export const EVENTO_NOTIFICACOES = 'notificacoes-mudaram'

const filtros: { valor: boolean | ''; rotulo: string }[] = [
  { valor: '', rotulo: 'Todas' },
  { valor: false, rotulo: 'Não lidas' },
  { valor: true, rotulo: 'Lidas' },
]

export default function Notificacoes() {
  const [notificacoes, setNotificacoes] = useState<NotificacaoResponse[]>([])
  const [total, setTotal] = useState(0)
  const [totalPaginas, setTotalPaginas] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  const [lida, setLida] = useState<boolean | ''>('')
  const [pagina, setPagina] = useState(0)
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setCarregando(true)
    setErro('')
    try {
      const resposta = await listarNotificacoes(lida, pagina)
      setNotificacoes(resposta.conteudo)
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
  }, [lida, pagina])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  function filtrar(valor: boolean | '') {
    setLida(valor)
    setPagina(0)
  }

  function aoMudar(mensagem: string) {
    setAviso(mensagem)
    window.dispatchEvent(new Event(EVENTO_NOTIFICACOES))
    carregar()
  }

  async function marcar(id: number) {
    setErro('')
    try {
      await marcarComoLida(id)
      aoMudar('Notificação marcada como lida')
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível atualizar')
    }
  }

  async function marcarTodas() {
    setErro('')
    try {
      await marcarTodasComoLidas()
      aoMudar('Todas marcadas como lidas')
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível atualizar')
    }
  }

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <div className="flex flex-wrap gap-2">
          {filtros.map((f) => (
            <button
              key={f.rotulo}
              className={`chip ${lida === f.valor ? 'on' : ''}`}
              onClick={() => filtrar(f.valor)}
            >
              {f.rotulo}
            </button>
          ))}
        </div>

        <div className="flex-1" />

        <button className="btn" onClick={marcarTodas}>
          <CheckCheck size={16} />
          Marcar todas como lidas
        </button>
      </div>

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      <div className="card overflow-hidden">
        {carregando && notificacoes.length === 0 && (
          <div className="py-8 text-center text-muted">Carregando...</div>
        )}

        {!carregando && notificacoes.length === 0 && (
          <div className="py-8 text-center text-muted">Nenhuma notificação</div>
        )}

        {notificacoes.map((n) => (
          <div
            key={n.idNotificacao}
            className={`flex items-center gap-3 border-b-[1.5px] border-line-soft px-4 py-3 last:border-b-0 ${
              n.lida ? '' : 'bg-accent-soft'
            }`}
          >
            <Bell size={16} className={`flex-none ${n.lida ? 'text-muted' : 'text-accent-ink'}`} />

            <div className="min-w-0 flex-1">
              <div className={n.lida ? 'text-ink-soft' : 'font-semibold'}>{n.mensagem}</div>
              <div className="text-xs text-muted">{formatarDataHora(n.geradaEm)}</div>
            </div>

            {n.idTarefa && (
              <Link to="/tarefas" className="text-xs text-accent-ink hover:underline">
                Ver tarefas
              </Link>
            )}

            {!n.lida && (
              <button className="btn btn-sm" onClick={() => marcar(n.idNotificacao)}>
                Marcar como lida
              </button>
            )}
          </div>
        ))}

        <div className="flex items-center justify-between border-t-2 border-line px-4 py-2.5 text-xs text-muted">
          <span>
            {total} {total === 1 ? 'notificação' : 'notificações'}
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

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
