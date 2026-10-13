import type { StatusCliente, TipoInteracao } from '../../api/tipos'

export const rotuloStatus: Record<StatusCliente, string> = {
  PROSPECT: 'Prospect',
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
}

export const corDoStatus: Record<StatusCliente, string> = {
  PROSPECT: 'badge-amber',
  ATIVO: 'badge-green',
  INATIVO: 'badge-slate',
}

export const rotuloInteracao: Record<TipoInteracao, string> = {
  LIGACAO: 'Ligação',
  EMAIL: 'E-mail',
  REUNIAO: 'Reunião',
}

export function formatarDataHora(iso: string) {
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

// o input datetime-local quer o horario local no formato 2026-10-13T18:30.
export function agoraParaInput() {
  const agora = new Date()
  agora.setMinutes(agora.getMinutes() - agora.getTimezoneOffset())
  return agora.toISOString().slice(0, 16)
}
