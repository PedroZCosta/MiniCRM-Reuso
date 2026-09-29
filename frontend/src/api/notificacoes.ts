import { requisitar } from './http'
import type { ContadorNotificacoesResponse, NotificacaoResponse, PageResponse } from './tipos'

// lida vazio traz todas; true so as lidas; false so as nao lidas.
export function listarNotificacoes(lida: boolean | '', pagina: number) {
  const params = new URLSearchParams({ page: String(pagina) })
  if (lida !== '') params.set('lida', String(lida))

  return requisitar<PageResponse<NotificacaoResponse>>(`/notificacoes?${params}`)
}

// numero do sino no menu.
export function contarNaoLidas() {
  return requisitar<ContadorNotificacoesResponse>('/notificacoes/contador')
}

export function marcarComoLida(id: number) {
  return requisitar<NotificacaoResponse>(`/notificacoes/${id}/lida`, { method: 'PATCH' })
}

export function marcarTodasComoLidas() {
  return requisitar<ContadorNotificacoesResponse>('/notificacoes/marcar-todas', { method: 'PATCH' })
}
