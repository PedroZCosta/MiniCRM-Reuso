import { Outlet, useNavigate } from 'react-router'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import { sair } from '../../api/auth'
import { sessao } from '../../api/http'
import type { UsuarioResponse } from '../../api/tipos'

export default function AppLayout() {
  const navegar = useNavigate()
  const usuario = sessao.usuario() as UsuarioResponse | null

  function encerrar() {
    sair()
    navegar('/login', { replace: true })
  }

  return (
    <div className="grid min-h-screen grid-cols-[172px_1fr]">
      <Sidebar
        nome={usuario?.nome ?? 'Visitante'}
        perfil={usuario?.perfil ?? ''}
        aoSair={encerrar}
      />

      <div className="flex min-w-0 flex-col">
        <Topbar />

        {/* o Outlet e o buraco onde a tela de cada integrante aparece. */}
        <div className="p-6">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
