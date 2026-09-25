import { Outlet, useNavigate } from 'react-router'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function AppLayout() {
  const navegar = useNavigate()

  // por enquanto so volta para o login; limpar o token entra depois.
  function sair() {
    navegar('/login')
  }

  return (
    <div className="grid min-h-screen grid-cols-[172px_1fr]">
      <Sidebar nome="Pedro Costa" perfil="ADMIN" aoSair={sair} />

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
