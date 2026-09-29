import { useEffect, useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { listarClientes } from '../../api/clientes'
import { criarTarefa, editarTarefa } from '../../api/tarefas'
import type { ClienteResponse, TarefaResponse, UsuarioResponse } from '../../api/tipos'

type Props = {
  // preenchido e edicao; vazio e criacao.
  tarefa?: TarefaResponse
  // so chega preenchido para gerente e admin, que escolhem o responsavel.
  responsaveis: UsuarioResponse[]
  aoFechar: () => void
  aoSalvar: () => void
}

export default function TarefaFormulario({ tarefa, responsaveis, aoFechar, aoSalvar }: Props) {
  const editando = Boolean(tarefa)

  const [titulo, setTitulo] = useState(tarefa?.titulo ?? '')
  const [descricao, setDescricao] = useState(tarefa?.descricao ?? '')
  const [dataVencimento, setDataVencimento] = useState(tarefa?.dataVencimento ?? '')
  const [idCliente, setIdCliente] = useState('')
  const [idUsuario, setIdUsuario] = useState('')
  const [clientes, setClientes] = useState<ClienteResponse[]>([])
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  // cliente e responsavel so se escolhem na criacao: o backend nao deixa trocar na edicao.
  useEffect(() => {
    if (editando) return
    listarClientes({ busca: '', status: '', vendedorId: '', incluirExcluidos: false, pagina: 0 })
      .then((pagina) => setClientes(pagina.conteudo))
      .catch(() => setClientes([]))
  }, [editando])

  async function salvar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')

    if (!titulo.trim()) return setErro('Informe o título')
    if (!dataVencimento) return setErro('Informe a data de vencimento')

    setSalvando(true)
    const dados = { titulo: titulo.trim(), descricao, dataVencimento }
    try {
      if (editando && tarefa) {
        await editarTarefa(tarefa.idTarefa, dados)
      } else {
        await criarTarefa({
          ...dados,
          idCliente: idCliente ? Number(idCliente) : undefined,
          idUsuario: idUsuario ? Number(idUsuario) : undefined,
        })
      }
      aoSalvar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível salvar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo={editando ? 'Editar tarefa' : 'Nova tarefa'}
      aoFechar={aoFechar}
      rodape={
        <>
          <button type="button" className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button type="submit" form="form-tarefa" className="btn btn-pri" disabled={salvando}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
        </>
      }
    >
      <form id="form-tarefa" onSubmit={salvar} className="flex flex-col gap-3.5" noValidate>
        <div className="field">
          <label htmlFor="titulo-tarefa">Título *</label>
          <input
            id="titulo-tarefa"
            className="input"
            value={titulo}
            maxLength={120}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ligar para o cliente"
            autoFocus
          />
        </div>

        <div className="field">
          <label htmlFor="descricao-tarefa">Descrição</label>
          <textarea
            id="descricao-tarefa"
            className="input textarea"
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="vencimento-tarefa">Vencimento *</label>
          <input
            id="vencimento-tarefa"
            className="input"
            type="date"
            value={dataVencimento}
            onChange={(e) => setDataVencimento(e.target.value)}
          />
        </div>

        {!editando && (
          <div className="field">
            <label htmlFor="cliente-tarefa">Cliente</label>
            <select
              id="cliente-tarefa"
              className="select"
              value={idCliente}
              onChange={(e) => setIdCliente(e.target.value)}
            >
              <option value="">Sem cliente</option>
              {clientes.map((c) => (
                <option key={c.idCliente} value={c.idCliente}>
                  {c.nome}
                </option>
              ))}
            </select>
          </div>
        )}

        {!editando && responsaveis.length > 0 && (
          <div className="field">
            <label htmlFor="responsavel-tarefa">Responsável</label>
            <select
              id="responsavel-tarefa"
              className="select"
              value={idUsuario}
              onChange={(e) => setIdUsuario(e.target.value)}
            >
              <option value="">Eu mesmo</option>
              {responsaveis.map((u) => (
                <option key={u.idUsuario} value={u.idUsuario}>
                  {u.nome}
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
      </form>
    </Modal>
  )
}
