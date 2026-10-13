import { useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { transferirCliente } from '../../api/clientes'
import type { ClienteResponse, UsuarioResponse } from '../../api/tipos'

type Props = {
  cliente: ClienteResponse
  vendedores: UsuarioResponse[]
  aoFechar: () => void
  aoSalvar: () => void
}

export default function TransferirCliente({ cliente, vendedores, aoFechar, aoSalvar }: Props) {
  const outros = vendedores.filter((v) => v.idUsuario !== cliente.vendedor.idUsuario)

  const [novoVendedor, setNovoVendedor] = useState(outros[0]?.idUsuario.toString() ?? '')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function salvar() {
    setErro('')
    setSalvando(true)
    try {
      await transferirCliente(cliente.idCliente, Number(novoVendedor))
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível transferir')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo="Transferir cliente"
      aoFechar={aoFechar}
      rodape={
        <>
          <button className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button className="btn btn-pri" onClick={salvar} disabled={salvando || !novoVendedor}>
            {salvando ? 'Transferindo...' : 'Transferir'}
          </button>
        </>
      }
    >
      <div className="modal-note">
        Hoje <b>{cliente.nome}</b> está com <b>{cliente.vendedor.nome}</b>. O histórico e as
        oportunidades continuam com o cliente.
      </div>

      {outros.length === 0 ? (
        <p className="text-sm text-muted">Não há outro vendedor ativo para receber o cliente.</p>
      ) : (
        <div className="field">
          <label htmlFor="novo-vendedor">Novo vendedor</label>
          <select
            id="novo-vendedor"
            className="select"
            value={novoVendedor}
            onChange={(e) => setNovoVendedor(e.target.value)}
          >
            {outros.map((v) => (
              <option key={v.idUsuario} value={v.idUsuario}>
                {v.nome}
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
    </Modal>
  )
}
