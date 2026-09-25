import { useState } from 'react'
import Modal from '../Modal'
import { ErroApi } from '../../api/http'
import { alterarPerfil, criarUsuario, editarUsuario } from '../../api/usuarios'
import type { PerfilUsuario, UsuarioResponse } from '../../api/tipos'

// RN-05: quem cria quem. o admin nao cria vendedor, so o gerente cria.
const perfisQuePodeCriar: Record<PerfilUsuario, PerfilUsuario[]> = {
  ADMIN: ['ADMIN', 'GERENTE'],
  GERENTE: ['VENDEDOR'],
  VENDEDOR: [],
}

type Props = {
  // quando vem preenchido e edicao; quando vem vazio e criacao.
  usuario?: UsuarioResponse
  perfilDeQuemCria: PerfilUsuario
  aoFechar: () => void
  aoSalvar: (senhaProvisoria?: string) => void
}

export default function UsuarioFormulario({
  usuario,
  perfilDeQuemCria,
  aoFechar,
  aoSalvar,
}: Props) {
  const editando = Boolean(usuario)
  const opcoes = perfisQuePodeCriar[perfilDeQuemCria]

  const [nome, setNome] = useState(usuario?.nome ?? '')
  const [email, setEmail] = useState(usuario?.email ?? '')
  const [perfil, setPerfil] = useState<PerfilUsuario>(usuario?.perfil ?? opcoes[0] ?? 'VENDEDOR')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function salvar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')
    setSalvando(true)

    try {
      if (editando && usuario) {
        await editarUsuario(usuario.idUsuario, nome, email)

        // o perfil tem endpoint proprio, entao so chama se mudou mesmo.
        if (perfil !== usuario.perfil) {
          await alterarPerfil(usuario.idUsuario, perfil)
        }
        aoSalvar()
      } else {
        const criado = await criarUsuario(nome, email, perfil)
        aoSalvar(criado.senhaProvisoria)
      }
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível salvar')
      setSalvando(false)
    }
  }

  return (
    <Modal
      titulo={editando ? 'Editar usuário' : 'Novo usuário'}
      aoFechar={aoFechar}
      rodape={
        <>
          <button type="button" className="btn" onClick={aoFechar}>
            Cancelar
          </button>
          <button type="submit" form="form-usuario" className="btn btn-pri" disabled={salvando}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
        </>
      }
    >
      <form id="form-usuario" onSubmit={salvar} className="flex flex-col gap-3.5" noValidate>
        <div className="field">
          <label htmlFor="nome">Nome</label>
          <input
            id="nome"
            className="input"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="Nome completo"
            autoFocus
          />
        </div>

        <div className="field">
          <label htmlFor="email-usuario">E-mail</label>
          <input
            id="email-usuario"
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="pessoa@email.com"
          />
        </div>

        <div className="field">
          <label htmlFor="perfil">Perfil</label>
          <select
            id="perfil"
            className="select"
            value={perfil}
            onChange={(e) => setPerfil(e.target.value as PerfilUsuario)}
          >
            {/* na edicao pode aparecer o perfil atual mesmo fora da lista de criacao. */}
            {[...new Set([...opcoes, ...(editando && usuario ? [usuario.perfil] : [])])].map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </div>

        {erro && (
          <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
            {erro}
          </div>
        )}

        {!editando && (
          <div className="modal-note">
            O sistema gera uma senha provisória e exige a troca no primeiro acesso. Ela aparece uma
            única vez, logo depois de salvar.
          </div>
        )}
      </form>
    </Modal>
  )
}
