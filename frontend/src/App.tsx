import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './components/layout/AppLayout'
import RotaProtegida from './components/RotaProtegida'
import Login from './pages/Login'
import RecuperarSenha from './pages/RecuperarSenha'
import TrocarSenha from './pages/TrocarSenha'
import Usuarios from './pages/Usuarios'

export default function App() {
  return (
    <Routes>
      {/* telas sem casco: nao tem menu nem barra de cima. */}
      <Route path="/login" element={<Login />} />
      <Route path="/recuperar-senha" element={<RecuperarSenha />} />

      {/* exige token, mas ainda sem menu: a pessoa tem que trocar a senha antes. */}
      <Route
        path="/trocar-senha"
        element={
          <RotaProtegida>
            <TrocarSenha />
          </RotaProtegida>
        }
      />

      {/* tudo aqui dentro exige token e ganha sidebar + topbar. */}
      <Route
        element={
          <RotaProtegida>
            <AppLayout />
          </RotaProtegida>
        }
      >
        <Route path="/" element={<Navigate to="/usuarios" replace />} />
        <Route path="/usuarios" element={<Usuarios />} />
      </Route>
    </Routes>
  )
}
