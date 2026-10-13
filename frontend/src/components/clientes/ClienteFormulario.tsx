import { useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { criarCliente, editarCliente } from '../../api/clientes'
import type { ClienteResponse, UsuarioResponse } from '../../api/tipos'

type Props = {
  // preenchido e edicao; vazio e criacao.
  cliente?: ClienteResponse
  // so chega preenchido para gerente e admin, que escolhem o vendedor.
  vendedores: UsuarioResponse[]
  aoFechar: () => void
  aoSalvar: () => void
}

export default function ClienteFormulario({ cliente, vendedores, aoFechar, aoSalvar }: Props) {
  const editando = Boolean(cliente)

  const [nome, setNome] = useState(cliente?.nome ?? '')
  const [email, setEmail] = useState(cliente?.email ?? '')
  const [telefone, setTelefone] = useState(cliente?.telefone ?? '')
  const [empresa, setEmpresa] = useState(cliente?.empresa ?? '')
  const [idVendedor, setIdVendedor] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function salvar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')
    setSalvando(true)

    const dados = { nome, email, telefone, empresa }
    try {
      if (editando && cliente) {
        await editarCliente(cliente.idCliente, dados)
      } else {
        await criarCliente(dados, idVendedor ? Number(idVendedor) : undefined)
      }
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível salvar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo={editando ? 'Editar cliente' : 'Novo cliente'}
      aoFechar={aoFechar}
      rodape={
        <>
          <button type="button" className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button type="submit" form="form-cliente" className="btn btn-pri" disabled={salvando}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
        </>
      }
    >
      <form id="form-cliente" onSubmit={salvar} className="flex flex-col gap-3.5" noValidate>
        <div className="field">
          <label htmlFor="nome-cliente">Nome *</label>
          <input
            id="nome-cliente"
            className="input"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="Nome do cliente"
            autoFocus
          />
        </div>

        <div className="field">
          <label htmlFor="email-cliente">E-mail *</label>
          <input
            id="email-cliente"
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="contato@empresa.com"
          />
        </div>

        <div className="grid grid-cols-2 gap-3.5">
          <div className="field">
            <label htmlFor="telefone-cliente">Telefone</label>
            <input
              id="telefone-cliente"
              className="input"
              value={telefone}
              onChange={(e) => setTelefone(e.target.value)}
              placeholder="41999990000"
            />
          </div>

          <div className="field">
            <label htmlFor="empresa-cliente">Empresa</label>
            <input
              id="empresa-cliente"
              className="input"
              value={empresa}
              onChange={(e) => setEmpresa(e.target.value)}
            />
          </div>
        </div>

        {!editando && vendedores.length > 0 && (
          <div className="field">
            <label htmlFor="vendedor-cliente">Vendedor responsável</label>
            <select
              id="vendedor-cliente"
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

        {!editando && (
          <div className="modal-note">
            Todo cliente começa como Prospect e vira Ativo sozinho quando a primeira oportunidade
            dele é ganha.
          </div>
        )}
      </form>
    </Modal>
  )
}
