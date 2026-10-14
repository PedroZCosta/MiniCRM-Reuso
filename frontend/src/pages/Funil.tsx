import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { Plus } from 'lucide-react'
import FecharOportunidade from '../components/funil/FecharOportunidade'
import OportunidadeFormulario from '../components/funil/OportunidadeFormulario'
import { formatarData, formatarReais, rotuloEtapa } from '../components/funil/formato'
import { listarVendedoresAtivos } from '../api/clientes'
import { buscarFunil, moverEtapa } from '../api/funil'
import { ErroApi, sessao } from '../api/http'
import type { ColunaFunil, FunilResponse, OportunidadeResponse, UsuarioResponse } from '../api/tipos'

const corDaColuna: Record<ColunaFunil['etapa'], string> = {
  PROSPECCAO: '#6b6b63',
  CONTATO: '#2a78d6',
  PROPOSTA: '#b05a2a',
  FECHADO: '#2f8f5b',
}

export default function Funil() {
  const navegar = useNavigate()
  const logado = sessao.usuario() as UsuarioResponse
  const ehGestor = logado.perfil !== 'VENDEDOR'

  const [funil, setFunil] = useState<FunilResponse | null>(null)
  const [vendedores, setVendedores] = useState<UsuarioResponse[]>([])
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  const [criando, setCriando] = useState(false)
  const [fechando, setFechando] = useState<OportunidadeResponse | null>(null)
  const [arrastando, setArrastando] = useState<OportunidadeResponse | null>(null)
  const [colunaSobre, setColunaSobre] = useState('')

  async function carregar() {
    try {
      setFunil(await buscarFunil())
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    }
  }

  useEffect(() => {
    carregar()
    if (ehGestor) listarVendedoresAtivos().then(setVendedores).catch(() => setVendedores([]))
  }, [])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  // soltar em Fechado abre a escolha entre ganha e perdida; nas outras colunas ja move.
  async function soltar(coluna: ColunaFunil['etapa']) {
    setColunaSobre('')
    const oportunidade = arrastando
    setArrastando(null)
    if (!oportunidade || oportunidade.etapa === coluna) return

    if (coluna === 'FECHADO') {
      if (oportunidade.resultado === null) setFechando(oportunidade)
      return
    }

    setErro('')
    try {
      await moverEtapa(oportunidade.idOportunidade, coluna)
      setAviso(`Movida para ${rotuloEtapa[coluna]}`)
      carregar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível mover')
    }
  }

  function aoSalvar(mensagem: string) {
    setCriando(false)
    setFechando(null)
    setAviso(mensagem)
    carregar()
  }

  const total = funil?.colunas.reduce((soma, c) => soma + c.quantidade, 0) ?? 0

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <span className="text-[13px] font-semibold text-muted">
          {total} oportunidades · {ehGestor ? 'visão da equipe' : 'minha carteira'}
        </span>
        {funil && (
          <span className="badge badge-signal">
            Em negociação: {formatarReais(funil.valorEmNegociacao)}
          </span>
        )}

        <div className="flex-1" />

        <button className="btn btn-pri" onClick={() => setCriando(true)}>
          <Plus size={16} />
          Nova oportunidade
        </button>
      </div>

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      {!funil && !erro && <div className="card py-12 text-center text-muted">Carregando...</div>}

      {funil && (
        <div className="kanban">
          {funil.colunas.map((coluna) => (
            <div
              key={coluna.etapa}
              data-coluna={coluna.etapa}
              className={`kcol ${colunaSobre === coluna.etapa ? 'sobre' : ''}`}
              onDragOver={(e) => {
                e.preventDefault()
                setColunaSobre(coluna.etapa)
              }}
              onDragLeave={() => setColunaSobre('')}
              onDrop={() => soltar(coluna.etapa)}
            >
              <div className="kcol-head">
                <span className="dotstage" style={{ background: corDaColuna[coluna.etapa] }} />
                <b style={{ color: corDaColuna[coluna.etapa] }}>{rotuloEtapa[coluna.etapa]}</b>
                <span className="ml-auto text-[11px] text-muted">{coluna.quantidade}</span>
              </div>
              <div className="px-1.5 pb-2.5 font-hand text-[15px] text-ink-soft">
                {formatarReais(coluna.valorTotal)}
              </div>

              <div className="flex flex-col gap-[9px]">
                {coluna.oportunidades.length === 0 && <div className="kdrop-hint">soltar aqui</div>}

                {coluna.oportunidades.map((o) => (
                  <div
                    key={o.idOportunidade}
                    data-oportunidade={o.idOportunidade}
                    className={`kcard ${o.etapa === 'GANHA' ? 'ganha' : ''} ${o.etapa === 'PERDIDA' ? 'perdida' : ''}`}
                    draggable={o.resultado === null}
                    onDragStart={() => setArrastando(o)}
                    onClick={() => navegar(`/funil/${o.idOportunidade}`)}
                  >
                    <div className="text-[11.5px] font-semibold text-muted">{o.cliente}</div>
                    <div className="mt-0.5 font-hand text-base">{o.titulo}</div>
                    <div className="mt-1.5 text-[13px] font-bold">
                      {o.etapa === 'GANHA' && '✓ Ganha · '}
                      {o.etapa === 'PERDIDA' && '✕ Perdida · '}
                      {formatarReais(o.valorEstimado)}
                    </div>
                    <div className="mt-2 flex justify-between text-[11px] text-muted">
                      <span>{o.vendedor}</span>
                      {o.dataPrevista && <span>{formatarData(o.dataPrevista)}</span>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="mt-3.5 text-xs text-muted">
        Arraste um card entre as colunas para mudar a etapa. Ao soltar em "Fechado", escolha Ganha
        ou Perdida. Clique no card para ver os detalhes.
      </p>

      {criando && (
        <OportunidadeFormulario
          vendedores={vendedores}
          aoFechar={() => setCriando(false)}
          aoSalvar={() => aoSalvar('Oportunidade criada')}
        />
      )}

      {fechando && (
        <FecharOportunidade
          oportunidade={fechando}
          aoFechar={() => setFechando(null)}
          aoSalvar={() => aoSalvar('Oportunidade fechada')}
        />
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
