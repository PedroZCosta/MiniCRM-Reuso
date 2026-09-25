import { Plus, Pencil, Search } from 'lucide-react'
import Avatar from '../components/Avatar'

// dados de mentira ate a chamada em GET /api/v1/usuarios entrar no lugar.
const usuariosFalsos = [
  { idUsuario: 1, nome: 'Pedro Costa', email: 'teste@email.com', perfil: 'ADMIN', ativo: true },
  { idUsuario: 2, nome: 'Lincoln Neto', email: 'lincoln@email.com', perfil: 'GERENTE', ativo: true },
  { idUsuario: 3, nome: 'Leandro Canha', email: 'leandro@email.com', perfil: 'VENDEDOR', ativo: true },
  { idUsuario: 4, nome: 'Nicolas Hara', email: 'nicolas@email.com', perfil: 'VENDEDOR', ativo: false },
]

// cada perfil tem a sua cor de etiqueta.
const corDoPerfil: Record<string, string> = {
  ADMIN: 'badge-violet',
  GERENTE: 'badge-signal',
  VENDEDOR: 'badge-slate',
}

export default function Usuarios() {
  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2.5">
        <div className="relative max-w-[340px] min-w-[220px] flex-1">
          <Search size={15} className="absolute top-1/2 left-3 -translate-y-1/2 text-muted" />
          <input className="input h-[39px] w-full pl-9" placeholder="Buscar por nome ou e-mail" />
        </div>

        <div className="flex-1" />

        <span className="text-xs text-muted">
          {usuariosFalsos.length} usuários · 3 perfis
        </span>

        <button className="btn btn-pri">
          <Plus size={16} />
          Novo usuário
        </button>
      </div>

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
            {usuariosFalsos.map((usuario) => (
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
                    <button className="btn btn-sm">
                      <Pencil size={14} />
                      Editar
                    </button>
                    <button className={`btn btn-sm ${usuario.ativo ? 'btn-danger' : ''}`}>
                      {usuario.ativo ? 'Desativar' : 'Reativar'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex items-center justify-between border-t-2 border-line px-4 py-2.5 text-xs text-muted">
          <span>Mostrando {usuariosFalsos.length} de {usuariosFalsos.length}</span>
          <div className="flex gap-1">
            <button className="h-7 w-7 rounded-[5px] border-2 border-line bg-line text-xs font-semibold text-white">
              1
            </button>
          </div>
        </div>
      </div>
    </>
  )
}
