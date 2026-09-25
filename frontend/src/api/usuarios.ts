import { requisitar } from './http'
import type { PageResponse, UsuarioResponse } from './tipos'

export function listarUsuarios(pagina = 0) {
  return requisitar<PageResponse<UsuarioResponse>>(`/usuarios?page=${pagina}`)
}

export function desativarUsuario(id: number) {
  return requisitar<void>(`/usuarios/${id}/desativar`, { method: 'PATCH' })
}

export function reativarUsuario(id: number) {
  return requisitar<void>(`/usuarios/${id}/reativar`, { method: 'PATCH' })
}
