import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './components/layout/AppLayout'
import RotaProtegida from './components/RotaProtegida'
import ClienteDetalhe from './pages/ClienteDetalhe'
import Clientes from './pages/Clientes'
import Funil from './pages/Funil'
import Login from './pages/Login'
import Notificacoes from './pages/Notificacoes'
import OportunidadeDetalhe from './pages/OportunidadeDetalhe'
import RecuperarSenha from './pages/RecuperarSenha'
import Tarefas from './pages/Tarefas'
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
        <Route path="/clientes" element={<Clientes />} />
        <Route path="/clientes/:id" element={<ClienteDetalhe />} />
        <Route path="/funil" element={<Funil />} />
        <Route path="/funil/:id" element={<OportunidadeDetalhe />} />
        <Route path="/tarefas" element={<Tarefas />} />
        <Route path="/notificacoes" element={<Notificacoes />} />
      </Route>
    </Routes>
  )
}
