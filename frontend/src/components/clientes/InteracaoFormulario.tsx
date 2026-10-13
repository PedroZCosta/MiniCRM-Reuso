import { useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { registrarInteracao } from '../../api/clientes'
import type { ClienteResponse, TipoInteracao } from '../../api/tipos'
import { agoraParaInput, rotuloInteracao } from './formato'

type Props = {
  cliente: ClienteResponse
  aoFechar: () => void
  aoSalvar: () => void
}

export default function InteracaoFormulario({ cliente, aoFechar, aoSalvar }: Props) {
  const [tipo, setTipo] = useState<TipoInteracao>('LIGACAO')
  const [data, setData] = useState(agoraParaInput())
  const [observacoes, setObservacoes] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function salvar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')
    setSalvando(true)

    try {
      await registrarInteracao(cliente.idCliente, tipo, data, observacoes)
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível registrar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo="Registrar interação"
      aoFechar={aoFechar}
      rodape={
        <>
          <button type="button" className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button type="submit" form="form-interacao" className="btn btn-pri" disabled={salvando}>
            {salvando ? 'Salvando...' : 'Registrar'}
          </button>
        </>
      }
    >
      <form id="form-interacao" onSubmit={salvar} className="flex flex-col gap-3.5" noValidate>
        <div className="modal-note">
          Cliente: <b>{cliente.nome}</b>
          {cliente.empresa && ` · ${cliente.empresa}`}
        </div>

        <div className="grid grid-cols-2 gap-3.5">
          <div className="field">
            <label htmlFor="tipo-interacao">Tipo</label>
            <select
              id="tipo-interacao"
              className="select"
              value={tipo}
              onChange={(e) => setTipo(e.target.value as TipoInteracao)}
            >
              {Object.entries(rotuloInteracao).map(([valor, rotulo]) => (
                <option key={valor} value={valor}>
                  {rotulo}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="data-interacao">Data</label>
            <input
              id="data-interacao"
              className="input"
              type="datetime-local"
              value={data}
              onChange={(e) => setData(e.target.value)}
            />
          </div>
        </div>

        <div className="field">
          <label htmlFor="obs-interacao">Observações</label>
          <textarea
            id="obs-interacao"
            className="input textarea"
            value={observacoes}
            onChange={(e) => setObservacoes(e.target.value)}
            placeholder="O que foi conversado?"
          />
        </div>

        {erro && (
          <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
            {erro}
          </div>
        )}
      </form>
    </Modal>
  )
}
