import { requisitar } from './http'
import type { FaixaTarefa, PageResponse, SituacaoTarefa, TarefaResponse } from './tipos'

// o backend nao aceita situacao e faixa juntas, entao a tela manda so um dos dois.
export type FiltroTarefas = {
  situacao: SituacaoTarefa | ''
  faixa: FaixaTarefa | ''
  pagina: number
}

export type DadosTarefa = {
  titulo: string
  descricao: string
  dataVencimento: string
}

export type DadosNovaTarefa = DadosTarefa & {
  idCliente?: number
  // so gerente e admin escolhem o responsavel; sem ele vale quem esta logado.
  idUsuario?: number
}

export function listarTarefas(filtro: FiltroTarefas) {
  const params = new URLSearchParams({ page: String(filtro.pagina) })
  if (filtro.situacao) params.set('situacao', filtro.situacao)
  if (filtro.faixa) params.set('faixa', filtro.faixa)

  return requisitar<PageResponse<TarefaResponse>>(`/tarefas?${params}`)
}

export function criarTarefa(dados: DadosNovaTarefa) {
  return requisitar<TarefaResponse>('/tarefas', {
    method: 'POST',
    body: JSON.stringify(dados),
  })
}

// editar so mexe em titulo, descricao e vencimento.
export function editarTarefa(id: number, dados: DadosTarefa) {
  return requisitar<TarefaResponse>(`/tarefas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dados),
  })
}

export function concluirTarefa(id: number) {
  return requisitar<TarefaResponse>(`/tarefas/${id}/concluir`, { method: 'PATCH' })
}

export function reabrirTarefa(id: number) {
  return requisitar<TarefaResponse>(`/tarefas/${id}/reabrir`, { method: 'PATCH' })
}
