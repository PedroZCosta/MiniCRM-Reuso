import { requisitar } from './http'
import { listarClientes } from './clientes'
import type {
  ClienteResponse,
  EtapaOportunidade,
  FunilResponse,
  MotivoPerdaResponse,
  OportunidadeDetalheResponse,
  OportunidadeResponse,
} from './tipos'

export type DadosOportunidade = {
  titulo: string
  valorEstimado: number
  dataPrevista: string | null
}

export function buscarFunil() {
  return requisitar<FunilResponse>('/funil')
}

export function buscarOportunidade(id: number) {
  return requisitar<OportunidadeDetalheResponse>(`/oportunidades/${id}`)
}

// sem idVendedor o backend usa quem esta logado.
export function criarOportunidade(idCliente: number, dados: DadosOportunidade, idVendedor?: number) {
  return requisitar<OportunidadeResponse>('/oportunidades', {
    method: 'POST',
    body: JSON.stringify({ idCliente, ...dados, idVendedor }),
  })
}

export function editarOportunidade(id: number, dados: DadosOportunidade) {
  return requisitar<OportunidadeResponse>(`/oportunidades/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dados),
  })
}

// para PERDIDA o backend exige o motivo.
export function moverEtapa(id: number, novaEtapa: EtapaOportunidade, idMotivoPerda?: number) {
  return requisitar<OportunidadeResponse>(`/oportunidades/${id}/etapa`, {
    method: 'PATCH',
    body: JSON.stringify({ novaEtapa, idMotivoPerda }),
  })
}

export function reabrirOportunidade(id: number) {
  return requisitar<OportunidadeResponse>(`/oportunidades/${id}/reabrir`, { method: 'PATCH' })
}

export function listarMotivosPerda() {
  return requisitar<MotivoPerdaResponse[]>('/motivos-perda')
}

// a listagem de clientes vem de 20 em 20, entao junta todas as paginas para o select.
export async function listarClientesAtivos() {
  const todos: ClienteResponse[] = []
  let pagina = 0
  let totalPaginas = 1
  while (pagina < totalPaginas) {
    const resposta = await listarClientes({
      busca: '',
      status: '',
      vendedorId: '',
      incluirExcluidos: false,
      pagina,
    })
    todos.push(...resposta.conteudo)
    totalPaginas = resposta.totalPaginas
    pagina++
  }
  return todos
}
