import { Navigate, useLocation } from 'react-router'
import { sessao } from '../api/http'
import type { UsuarioResponse } from '../api/tipos'

// sem token no navegador ninguem passa daqui.
export default function RotaProtegida({ children }: { children: React.ReactNode }) {
  const { pathname } = useLocation()
  const usuario = sessao.usuario() as UsuarioResponse | null

  if (!sessao.token() || !usuario) return <Navigate to="/login" replace />

  // quem esta com senha provisoria so pode ir para a troca de senha.
  if (usuario.trocarSenha && pathname !== '/trocar-senha') {
    return <Navigate to="/trocar-senha" replace />
  }

  return <>{children}</>
}
