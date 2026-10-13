import { useEffect, useState } from 'react'
import { Check, X } from 'lucide-react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { listarMotivosPerda, moverEtapa } from '../../api/funil'
import type { MotivoPerdaResponse, OportunidadeResponse } from '../../api/tipos'

type Props = {
  oportunidade: OportunidadeResponse
  aoFechar: () => void
  aoSalvar: () => void
}

export default function FecharOportunidade({ oportunidade, aoFechar, aoSalvar }: Props) {
  const [resultado, setResultado] = useState<'GANHA' | 'PERDIDA'>('GANHA')
  const [motivos, setMotivos] = useState<MotivoPerdaResponse[]>([])
  const [idMotivo, setIdMotivo] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    listarMotivosPerda().then(setMotivos).catch(() => setMotivos([]))
  }, [])

  async function confirmar() {
    setErro('')
    setSalvando(true)
    try {
      await moverEtapa(oportunidade.idOportunidade, resultado, idMotivo ? Number(idMotivo) : undefined)
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível fechar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo="Fechar oportunidade"
      aoFechar={aoFechar}
      rodape={
        <>
          <button className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button className="btn btn-pri" onClick={confirmar} disabled={salvando}>
            {salvando ? 'Salvando...' : 'Confirmar'}
          </button>
        </>
      }
    >
      <p className="text-sm text-ink-soft">
        Marcar <b>{oportunidade.titulo}</b> ({oportunidade.cliente}) como:
      </p>

      <div className="flex gap-2.5">
        <button
          className={`btn h-12 flex-1 ${resultado === 'GANHA' ? 'resultado-ganha' : ''}`}
          onClick={() => setResultado('GANHA')}
        >
          <Check size={16} />
          Ganha
        </button>
        <button
          className={`btn h-12 flex-1 ${resultado === 'PERDIDA' ? 'resultado-perdida' : ''}`}
          onClick={() => setResultado('PERDIDA')}
        >
          <X size={16} />
          Perdida
        </button>
      </div>

      {resultado === 'PERDIDA' && (
        <div className="field">
          <label htmlFor="motivo-perda">Motivo da perda *</label>
          <select
            id="motivo-perda"
            className="select"
            value={idMotivo}
            onChange={(e) => setIdMotivo(e.target.value)}
          >
            <option value="" disabled>
              Selecione...
            </option>
            {motivos.map((m) => (
              <option key={m.idMotivo} value={m.idMotivo}>
                {m.descricao}
              </option>
            ))}
          </select>
        </div>
      )}

      {erro && (
        <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
          {erro}
        </div>
      )}

      <div className="modal-note">
        Oportunidade fechada não muda mais de etapa nem é editada. Para mexer de novo, use Reabrir.
      </div>
    </Modal>
  )
}
