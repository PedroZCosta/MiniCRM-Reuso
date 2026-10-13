import { useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { excluirCliente } from '../../api/clientes'
import type { ClienteResponse } from '../../api/tipos'

type Props = {
  cliente: ClienteResponse
  aoFechar: () => void
  aoExcluir: () => void
}

export default function ExcluirCliente({ cliente, aoFechar, aoExcluir }: Props) {
  const [erro, setErro] = useState('')
  const [excluindo, setExcluindo] = useState(false)

  async function confirmar() {
    setErro('')
    setExcluindo(true)
    try {
      await excluirCliente(cliente.idCliente)
      aoExcluir()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível excluir')
      setExcluindo(false)
    }
  }

  return (
    <Modal
      titulo="Excluir cliente"
      aoFechar={aoFechar}
      rodape={
        <>
          <button className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button className="btn btn-danger" onClick={confirmar} disabled={excluindo}>
            {excluindo ? 'Excluindo...' : 'Excluir'}
          </button>
        </>
      }
    >
      <p className="text-sm text-ink-soft">
        Excluir <b>{cliente.nome}</b>? Ele some da lista de clientes e não recebe novas interações.
      </p>
      <div className="modal-note">
        Nada é apagado do banco: o histórico fica guardado e gerente ou admin ainda conseguem ver o
        cliente marcando "mostrar excluídos".
      </div>

      {erro && (
        <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
          {erro}
        </div>
      )}
    </Modal>
  )
}
