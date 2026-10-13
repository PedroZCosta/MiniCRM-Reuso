import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ArrowRight, Building2, Calendar, ChevronLeft, Flag, Pencil, RotateCcw, User } from 'lucide-react'
import FecharOportunidade from '../components/funil/FecharOportunidade'
import OportunidadeFormulario from '../components/funil/OportunidadeFormulario'
import {
  corDaEtapa,
  etapasAbertas,
  formatarData,
  formatarReais,
  rotuloEtapa,
} from '../components/funil/formato'
import { formatarDataHora } from '../components/clientes/formato'
import { buscarOportunidade, moverEtapa, reabrirOportunidade } from '../api/funil'
import { ErroApi } from '../api/http'
import type { EtapaOportunidade, OportunidadeDetalheResponse } from '../api/tipos'

export default function OportunidadeDetalhe() {
  const { id } = useParams()
  const idOportunidade = Number(id)

  const [detalhe, setDetalhe] = useState<OportunidadeDetalheResponse | null>(null)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [editando, setEditando] = useState(false)
  const [fechando, setFechando] = useState(false)

  async function carregar() {
    try {
      setDetalhe(await buscarOportunidade(idOportunidade))
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    }
  }

  useEffect(() => {
    carregar()
  }, [idOportunidade])

  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  async function executar(acao: () => Promise<unknown>, mensagem: string) {
    setErro('')
    try {
      await acao()
      setAviso(mensagem)
      carregar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível concluir')
    }
  }

  function aoSalvar(mensagem: string) {
    setEditando(false)
    setFechando(false)
    setAviso(mensagem)
    carregar()
  }

  const oportunidade = detalhe?.oportunidade

  const voltar = (
    <Link to="/funil" className="mb-3 inline-flex items-center gap-1 font-hand text-base text-muted hover:text-ink">
      <ChevronLeft size={16} />
      Funil
      {oportunidade && <span className="text-line-soft">/</span>}
      {oportunidade?.titulo}
    </Link>
  )

  if (!detalhe || !oportunidade) {
    return (
      <>
        {voltar}
        <div className="card py-12 text-center text-muted">{erro || 'Carregando...'}</div>
      </>
    )
  }

  const fechada = oportunidade.resultado !== null
  const outrasEtapas = etapasAbertas.filter((e) => e !== oportunidade.etapa)

  return (
    <>
      {voltar}

      <div className="card mb-[18px] flex flex-wrap items-center gap-4 px-5 py-[18px]">
        <div className="min-w-0 flex-1">
          <h2 className="flex items-center gap-2.5 text-[23px]">
            {oportunidade.titulo}
            <span className={`badge ${corDaEtapa[oportunidade.etapa]}`}>
              {rotuloEtapa[oportunidade.etapa]}
            </span>
          </h2>
          <div className="mt-1.5 flex flex-wrap gap-3.5 text-[13px] text-muted">
            <span className="flex items-center gap-1.5">
              <Building2 size={14} />
              {oportunidade.cliente}
            </span>
            <span className="flex items-center gap-1.5">
              <User size={14} />
              {oportunidade.vendedor}
            </span>
            {oportunidade.dataPrevista && (
              <span className="flex items-center gap-1.5">
                <Calendar size={14} />
                previsão {formatarData(oportunidade.dataPrevista)}
              </span>
            )}
            {oportunidade.motivoPerda && (
              <span className="flex items-center gap-1.5 text-red">
                <Flag size={14} />
                motivo: {oportunidade.motivoPerda}
              </span>
            )}
          </div>
        </div>

        <div className="text-right font-hand text-[26px]">{formatarReais(oportunidade.valorEstimado)}</div>
      </div>

      <div className="card mb-[18px] flex flex-wrap items-center gap-2 px-5 py-3.5">
        {fechada ? (
          <>
            <span className="text-sm text-ink-soft">Oportunidade fechada. Para mexer de novo:</span>
            <button
              className="btn"
              onClick={() => executar(() => reabrirOportunidade(idOportunidade), 'Oportunidade reaberta')}
            >
              <RotateCcw size={15} />
              Reabrir
            </button>
          </>
        ) : (
          <>
            <span className="font-hand text-base text-ink-soft">Mover para:</span>
            {outrasEtapas.map((etapa: EtapaOportunidade) => (
              <button
                key={etapa}
                className="btn btn-sm"
                onClick={() =>
                  executar(() => moverEtapa(idOportunidade, etapa), `Movida para ${rotuloEtapa[etapa]}`)
                }
              >
                <ArrowRight size={14} />
                {rotuloEtapa[etapa]}
              </button>
            ))}
            <div className="flex-1" />
            <button className="btn" onClick={() => setEditando(true)}>
              <Pencil size={15} />
              Editar
            </button>
            <button className="btn btn-pri" onClick={() => setFechando(true)}>
              Fechar
            </button>
          </>
        )}
      </div>

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      <h3 className="mb-2.5 text-[19px]">Histórico de etapas</h3>
      <div className="card px-[18px] py-1">
        {detalhe.historico.length === 0 && (
          <div className="py-10 text-center text-muted">Ainda não mudou de etapa</div>
        )}

        {detalhe.historico.map((h, i) => (
          <div key={i} className="tl-item">
            <div className="tl-ico">
              <ArrowRight size={16} />
            </div>
            <div className="min-w-0 flex-1">
              <div className="flex justify-between gap-2.5 font-bold">
                <span>
                  {rotuloEtapa[h.etapaAnterior]} → {rotuloEtapa[h.etapaNova]}
                </span>
                <time className="text-xs font-medium text-muted">{formatarDataHora(h.alteradoEm)}</time>
              </div>
              <div className="mt-1.5 text-[11.5px] text-muted">por {h.usuario}</div>
            </div>
          </div>
        ))}
      </div>

      {editando && (
        <OportunidadeFormulario
          oportunidade={oportunidade}
          vendedores={[]}
          aoFechar={() => setEditando(false)}
          aoSalvar={() => aoSalvar('Alterações salvas')}
        />
      )}

      {fechando && (
        <FecharOportunidade
          oportunidade={oportunidade}
          aoFechar={() => setFechando(false)}
          aoSalvar={() => aoSalvar('Oportunidade fechada')}
        />
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
