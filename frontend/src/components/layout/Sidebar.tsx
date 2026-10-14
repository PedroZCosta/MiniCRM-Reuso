import { useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router'
import { Users, Building2, BarChart3, CheckSquare, Bell, LogOut } from 'lucide-react'
import Avatar from '../Avatar'
import { contarNaoLidas } from '../../api/notificacoes'
import { EVENTO_NOTIFICACOES } from '../../pages/Notificacoes'

// cada integrante adiciona aqui o item da tela dele.
const itensMenu = [
  { para: '/usuarios', rotulo: 'Usuários', Icone: Users },
  { para: '/clientes', rotulo: 'Clientes', Icone: Building2 },
  { para: '/funil', rotulo: 'Funil', Icone: BarChart3 },
  { para: '/tarefas', rotulo: 'Tarefas', Icone: CheckSquare },
  { para: '/notificacoes', rotulo: 'Notificações', Icone: Bell },
]

type SidebarProps = {
  nome: string
  perfil: string
  aoSair: () => void
}

export default function Sidebar({ nome, perfil, aoSair }: SidebarProps) {
  const { pathname } = useLocation()
  const [naoLidas, setNaoLidas] = useState(0)

  // atualiza ao trocar de tela e quando a tela de notificacoes marca alguma como lida.
  useEffect(() => {
    function atualizar() {
      contarNaoLidas()
        .then((r) => setNaoLidas(r.naoLidas))
        .catch(() => setNaoLidas(0))
    }
    atualizar()
    window.addEventListener(EVENTO_NOTIFICACOES, atualizar)
    return () => window.removeEventListener(EVENTO_NOTIFICACOES, atualizar)
  }, [pathname])

  return (
    <aside className="sticky top-0 flex h-screen w-[172px] flex-col gap-[3px] border-r-2 border-line bg-surface-alt p-4 px-[11px]">
      <div className="flex items-center gap-2 px-1.5 pt-1.5 pb-[18px] font-hand text-[22px] text-ink">
        <span className="h-2.5 w-2.5 flex-none rounded-[3px] border-2 border-line bg-accent" />
        miniCRM
      </div>

      <nav className="flex flex-col gap-[3px]">
        {itensMenu.map(({ para, rotulo, Icone }) => (
          <NavLink
            key={para}
            to={para}
            // o NavLink descobre sozinho se a rota dele e a que esta aberta.
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <Icone size={16} className="flex-none opacity-80" />
            <span>{rotulo}</span>
            {para === '/notificacoes' && naoLidas > 0 && (
              <span className="ml-auto rounded-full bg-red px-1.5 font-body text-[11px] text-white">
                {naoLidas}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto border-t-2 border-dashed border-line-soft pt-2.5">
        <div className="flex items-center gap-2 p-1.5">
          <Avatar nome={nome} />
          <div className="min-w-0 flex-1">
            <b className="block truncate font-hand text-[15px] font-normal text-ink">{nome}</b>
            <span className="text-[11px] text-muted">{perfil}</span>
          </div>
          <button onClick={aoSair} title="Sair" className="p-1 text-muted hover:text-ink">
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  )
}
