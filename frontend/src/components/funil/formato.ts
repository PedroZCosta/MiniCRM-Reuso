import type { EtapaOportunidade } from '../../api/tipos'

export const rotuloEtapa: Record<EtapaOportunidade | 'FECHADO', string> = {
  PROSPECCAO: 'Prospecção',
  CONTATO: 'Contato',
  PROPOSTA: 'Proposta',
  GANHA: 'Ganha',
  PERDIDA: 'Perdida',
  FECHADO: 'Fechado',
}

export const corDaEtapa: Record<EtapaOportunidade, string> = {
  PROSPECCAO: 'badge-slate',
  CONTATO: 'badge-signal',
  PROPOSTA: 'badge-amber',
  GANHA: 'badge-green',
  PERDIDA: 'badge-red',
}

export const etapasAbertas: EtapaOportunidade[] = ['PROSPECCAO', 'CONTATO', 'PROPOSTA']

const reais = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function formatarReais(valor: number) {
  return reais.format(valor)
}

// a data vem como 2026-12-01; montar na mao evita o fuso mudar o dia.
export function formatarData(data: string) {
  return data.split('-').reverse().join('/')
}
