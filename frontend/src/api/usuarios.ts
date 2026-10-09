import { requisitar } from './http'
import type { PageResponse, PerfilUsuario, UsuarioResponse } from './tipos'

// a senha provisoria so vem nesta resposta, uma unica vez.
export type UsuarioCriarResponse = {
  usuario: UsuarioResponse
  senhaProvisoria: string
}

export function listarUsuarios(pagina = 0) {
  return requisitar<PageResponse<UsuarioResponse>>(`/usuarios?page=${pagina}`)
}

export function criarUsuario(nome: string, email: string, perfil: PerfilUsuario) {
  return requisitar<UsuarioCriarResponse>('/usuarios', {
    method: 'POST',
    body: JSON.stringify({ nome, email, perfil }),
  })
}

export function editarUsuario(id: number, nome: string, email: string) {
  return requisitar<UsuarioResponse>(`/usuarios/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ nome, email }),
  })
}

export function alterarPerfil(id: number, perfil: PerfilUsuario) {
  return requisitar<UsuarioResponse>(`/usuarios/${id}/perfil`, {
    method: 'PATCH',
    body: JSON.stringify({ perfil }),
  })
}

export function desativarUsuario(id: number) {
  return requisitar<void>(`/usuarios/${id}/desativar`, { method: 'PATCH' })
}

export function reativarUsuario(id: number) {
  return requisitar<void>(`/usuarios/${id}/reativar`, { method: 'PATCH' })
}
