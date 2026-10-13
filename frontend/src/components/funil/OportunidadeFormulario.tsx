import { useEffect, useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { criarOportunidade, editarOportunidade, listarClientesAtivos } from '../../api/funil'
import type { ClienteResponse, OportunidadeResponse, UsuarioResponse } from '../../api/tipos'

type Props = {
  // preenchido e edicao; vazio e criacao.
  oportunidade?: OportunidadeResponse
  // so chega preenchido para gerente e admin, que escolhem o vendedor.
  vendedores: UsuarioResponse[]
  aoFechar: () => void
  aoSalvar: () => void
}

export default function OportunidadeFormulario({ oportunidade, vendedores, aoFechar, aoSalvar }: Props) {
  const editando = Boolean(oportunidade)

  const [clientes, setClientes] = useState<ClienteResponse[]>([])
  const [idCliente, setIdCliente] = useState('')
  const [titulo, setTitulo] = useState(oportunidade?.titulo ?? '')
  const [valor, setValor] = useState(oportunidade ? String(oportunidade.valorEstimado) : '')
  const [dataPrevista, setDataPrevista] = useState(oportunidade?.dataPrevista ?? '')
  const [idVendedor, setIdVendedor] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    if (!editando) listarClientesAtivos().then(setClientes).catch(() => setClientes([]))
  }, [])

  async function salvar(evento: React.FormEvent) {
    evento.preventDefault()
    if (!editando && !idCliente) {
      setErro('Escolha o cliente')
      return
    }
    setErro('')
    setSalvando(true)

    const dados = {
      titulo,
      valorEstimado: Number(valor),
      dataPrevista: dataPrevista || null,
    }
    try {
      if (editando && oportunidade) {
        await editarOportunidade(oportunidade.idOportunidade, dados)
      } else {
        await criarOportunidade(Number(idCliente), dados, idVendedor ? Number(idVendedor) : undefined)
      }
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível salvar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo={editando ? 'Editar oportunidade' : 'Nova oportunidade'}
      aoFechar={aoFechar}
      rodape={
        <>
          <button type="button" className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button type="submit" form="form-oportunidade" className="btn btn-pri" disabled={salvando}>
            {salvando ? 'Salvando...' : editando ? 'Salvar' : 'Criar'}
          </button>
        </>
      }
    >
      <form id="form-oportunidade" onSubmit={salvar} className="flex flex-col gap-3.5" noValidate>
        {editando ? (
          <div className="modal-note">
            Cliente: <b>{oportunidade?.cliente}</b>
          </div>
        ) : (
          <div className="field">
            <label htmlFor="cliente-oportunidade">Cliente *</label>
            <select
              id="cliente-oportunidade"
              className="select"
              value={idCliente}
              onChange={(e) => setIdCliente(e.target.value)}
            >
              <option value="" disabled>
                Selecione...
              </option>
              {clientes.map((c) => (
                <option key={c.idCliente} value={c.idCliente}>
                  {c.nome}
                  {c.empresa && ` · ${c.empresa}`}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="field">
          <label htmlFor="titulo-oportunidade">Título *</label>
          <input
            id="titulo-oportunidade"
            className="input"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ex.: Plano mensal"
          />
        </div>

        <div className="grid grid-cols-2 gap-3.5">
          <div className="field">
            <label htmlFor="valor-oportunidade">Valor estimado (R$) *</label>
            <input
              id="valor-oportunidade"
              className="input"
              type="number"
              min="0"
              step="0.01"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="data-oportunidade">Previsão de fechamento</label>
            <input
              id="data-oportunidade"
              className="input"
              type="date"
              value={dataPrevista}
              onChange={(e) => setDataPrevista(e.target.value)}
            />
          </div>
        </div>

        {!editando && vendedores.length > 0 && (
          <div className="field">
            <label htmlFor="vendedor-oportunidade">Vendedor responsável</label>
            <select
              id="vendedor-oportunidade"
              className="select"
              value={idVendedor}
              onChange={(e) => setIdVendedor(e.target.value)}
            >
              <option value="">Eu mesmo</option>
              {vendedores.map((v) => (
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

        {!editando && <div className="modal-note">Toda oportunidade começa em Prospecção.</div>}
      </form>
    </Modal>
  )
}
