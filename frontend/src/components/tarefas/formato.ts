import type { SituacaoTarefa } from '../../api/tipos'

export const rotuloSituacao: Record<SituacaoTarefa, string> = {
  PENDENTE: 'Pendente',
  VENCIDA: 'Vencida',
  CONCLUIDA: 'Concluída',
}

export const corDaSituacao: Record<SituacaoTarefa, string> = {
  PENDENTE: 'badge-signal',
  VENCIDA: 'badge-red',
  CONCLUIDA: 'badge-green',
}

// o backend manda a data pura (2026-09-10). new Date() leria como UTC e mostraria o dia
// anterior no Brasil, entao a string e quebrada na mao.
export function formatarData(iso: string) {
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}
