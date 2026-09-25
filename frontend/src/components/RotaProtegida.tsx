import { Navigate } from 'react-router'
import { sessao } from '../api/http'

// sem token no navegador ninguem passa daqui.
export default function RotaProtegida({ children }: { children: React.ReactNode }) {
  if (!sessao.token()) return <Navigate to="/login" replace />
  return <>{children}</>
}
