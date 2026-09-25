import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './components/layout/AppLayout'
import RotaProtegida from './components/RotaProtegida'
import Login from './pages/Login'
import Usuarios from './pages/Usuarios'

export default function App() {
  return (
    <Routes>
      {/* o login fica fora do casco: nao tem menu nem barra de cima. */}
      <Route path="/login" element={<Login />} />

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
