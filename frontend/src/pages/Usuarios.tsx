import { useEffect, useState } from 'react'
import { Plus, Pencil, Search } from 'lucide-react'
import Avatar from '../components/Avatar'
import UsuarioFormulario from '../components/usuarios/UsuarioFormulario'
import SenhaProvisoria from '../components/usuarios/SenhaProvisoria'
import { desativarUsuario, listarUsuarios, reativarUsuario } from '../api/usuarios'
import { ErroApi, sessao } from '../api/http'
import type { UsuarioResponse } from '../api/tipos'

// cada perfil tem a sua cor de etiqueta.
const corDoPerfil: Record<string, string> = {
  ADMIN: 'badge-violet',
  GERENTE: 'badge-signal',
  VENDEDOR: 'badge-slate',
}

export default function Usuarios() {
  const logado = sessao.usuario() as UsuarioResponse

  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
  const [total, setTotal] = useState(0)
  const [busca, setBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  // quem esta aberto na tela agora.
  const [criando, setCriando] = useState(false)
  const [editando, setEditando] = useState<UsuarioResponse | null>(null)
  const [senhaNova, setSenhaNova] = useState('')
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setCarregando(true)
    setErro('')
    try {
      const pagina = await listarUsuarios(0)
      setUsuarios(pagina.conteudo)
      setTotal(pagina.totalRegistros)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    } finally {
      setCarregando(false)
    }
  }

  // o array vazio no fim faz isso rodar uma vez so, quando a tela abre.
  useEffect(() => {
    carregar()
  }, [])

  // o aviso verde some sozinho depois de tres segundos.
  useEffect(() => {
    if (!aviso) return
    const relogio = setTimeout(() => setAviso(''), 3000)
    return () => clearTimeout(relogio)
  }, [aviso])

  async function alternarSituacao(usuario: UsuarioResponse) {
    try {
      if (usuario.ativo) {
        await desativarUsuario(usuario.idUsuario)
        setAviso('Usuário desativado')
      } else {
        await reativarUsuario(usuario.idUsuario)
        setAviso('Usuário reativado')
      }
      // recarrega para a tela mostrar o que o banco realmente gravou.
      carregar()
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível alterar')
    }
  }

  function aoSalvarFormulario(senhaProvisoria?: string) {
    setCriando(false)
    setEditando(null)
    if (senhaProvisoria) setSenhaNova(senhaProvisoria)
    else setAviso('Alterações salvas')
    carregar()
  }

  // o filtro acontece aqui na tela, sobre a pagina que ja veio.
  const visiveis = usuarios.filter(
    (u) =>
      u.nome.toLowerCase().includes(busca.toLowerCase()) ||
      u.email.toLowerCase().includes(busca.toLowerCase()),
  )

  // vendedor nao cria ninguem, entao nem ve o botao.
  const podeCriar = logado?.perfil !== 'VENDEDOR'
  const podeMexerEmConta = logado?.perfil === 'ADMIN'

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <div className="relative max-w-[340px] min-w-[220px] flex-1">
          <Search size={15} className="absolute top-1/2 left-3 -translate-y-1/2 text-muted" />
          <input
            className="input h-[39px] w-full pl-9"
            placeholder="Buscar por nome ou e-mail"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>

        <div className="flex-1" />

        <span className="text-xs text-muted">{total} usuários · 3 perfis</span>

        {podeCriar && (
          <button className="btn btn-pri" onClick={() => setCriando(true)}>
            <Plus size={16} />
            Novo usuário
          </button>
        )}
      </div>

      {erro && (
        <div className="mb-4 rounded-[7px] border-2 border-dashed border-red bg-red-soft px-4 py-3 font-hand text-base text-red">
          {erro}
        </div>
      )}

      <div className="card overflow-hidden">
        <table className="dt">
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Situação</th>
              <th className="text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {carregando && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted">
                  Carregando...
                </td>
              </tr>
            )}

            {!carregando && visiveis.length === 0 && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted">
                  Nenhum usuário encontrado
                </td>
              </tr>
            )}

            {visiveis.map((usuario) => (
              // a key ajuda o react a saber qual linha e qual quando a lista muda.
              <tr key={usuario.idUsuario}>
                <td>
                  <div className="flex items-center gap-2.5">
                    <Avatar nome={usuario.nome} />
                    <span className="font-semibold">{usuario.nome}</span>
                  </div>
                </td>
                <td className="text-ink-soft">{usuario.email}</td>
                <td>
                  <span className={`badge ${corDoPerfil[usuario.perfil]}`}>{usuario.perfil}</span>
                </td>
                <td>
                  {usuario.ativo ? (
                    <span className="badge badge-green">Ativo</span>
                  ) : (
                    <span className="badge badge-slate">Inativo</span>
                  )}
                </td>
                <td>
                  <div className="flex justify-end gap-1">
                    {podeMexerEmConta && (
                      <>
                        <button className="btn btn-sm" onClick={() => setEditando(usuario)}>
                          <Pencil size={14} />
                          Editar
                        </button>

                        {/* ninguem desativa a propria conta (RN-07). */}
                        {usuario.idUsuario !== logado?.idUsuario && (
                          <button
                            onClick={() => alternarSituacao(usuario)}
                            className={`btn btn-sm ${usuario.ativo ? 'btn-danger' : ''}`}
                          >
                            {usuario.ativo ? 'Desativar' : 'Reativar'}
                          </button>
                        )}
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex items-center justify-between border-t-2 border-line px-4 py-2.5 text-xs text-muted">
          <span>
            Mostrando {visiveis.length} de {total}
          </span>
          <div className="flex gap-1">
            <button className="h-7 w-7 rounded-[5px] border-2 border-line bg-line text-xs font-semibold text-white">
              1
            </button>
          </div>
        </div>
      </div>

      {criando && (
        <UsuarioFormulario
          perfilDeQuemCria={logado.perfil}
          aoFechar={() => setCriando(false)}
          aoSalvar={aoSalvarFormulario}
        />
      )}

      {editando && (
        <UsuarioFormulario
          usuario={editando}
          perfilDeQuemCria={logado.perfil}
          aoFechar={() => setEditando(null)}
          aoSalvar={aoSalvarFormulario}
        />
      )}

      {senhaNova && <SenhaProvisoria senha={senhaNova} aoFechar={() => setSenhaNova('')} />}

      {aviso && <div className="toast">{aviso}</div>}
    </>
  )
}
