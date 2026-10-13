import { requisitar } from './http'
import { listarUsuarios } from './usuarios'
import type {
  ClienteResponse,
  HistoricoItem,
  InteracaoResponse,
  PageResponse,
  StatusCliente,
  TipoInteracao,
} from './tipos'

export type FiltroClientes = {
  busca: string
  status: StatusCliente | ''
  vendedorId: string
  incluirExcluidos: boolean
  pagina: number
}

export type DadosCliente = {
  nome: string
  email: string
  telefone: string
  empresa: string
}

export function listarClientes(filtro: FiltroClientes) {
  const params = new URLSearchParams({ page: String(filtro.pagina) })
  if (filtro.busca) params.set('busca', filtro.busca)
  if (filtro.status) params.set('status', filtro.status)
  if (filtro.vendedorId) params.set('vendedorId', filtro.vendedorId)
  if (filtro.incluirExcluidos) params.set('incluirExcluidos', 'true')

  return requisitar<PageResponse<ClienteResponse>>(`/clientes?${params}`)
}

export function buscarCliente(id: number) {
  return requisitar<ClienteResponse>(`/clientes/${id}`)
}

// sem idVendedor o backend usa quem esta logado.
export function criarCliente(dados: DadosCliente, idVendedor?: number) {
  return requisitar<ClienteResponse>('/clientes', {
    method: 'POST',
    body: JSON.stringify({ ...dados, idVendedor }),
  })
}

export function editarCliente(id: number, dados: DadosCliente) {
  return requisitar<ClienteResponse>(`/clientes/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dados),
  })
}

// exclusao logica: o cliente vira INATIVO e some da listagem.
export function excluirCliente(id: number) {
  return requisitar<void>(`/clientes/${id}`, { method: 'DELETE' })
}

export function transferirCliente(id: number, novoVendedorId: number) {
  return requisitar<ClienteResponse>(`/clientes/${id}/transferir`, {
    method: 'PATCH',
    body: JSON.stringify({ novoVendedorId }),
  })
}

export function listarInteracoes(idCliente: number, pagina = 0) {
  return requisitar<PageResponse<InteracaoResponse>>(
    `/clientes/${idCliente}/interacoes?page=${pagina}`,
  )
}

export function registrarInteracao(
  idCliente: number,
  tipo: TipoInteracao,
  dataInteracao: string,
  observacoes: string,
) {
  return requisitar<InteracaoResponse>(`/clientes/${idCliente}/interacoes`, {
    method: 'POST',
    body: JSON.stringify({ tipo, dataInteracao, observacoes }),
  })
}

export function historicoCliente(idCliente: number, pagina = 0) {
  return requisitar<PageResponse<HistoricoItem>>(`/clientes/${idCliente}/historico?page=${pagina}`)
}

// a lista de usuarios exige USUARIO_VER, entao so gerente e admin chamam isso.
export async function listarVendedoresAtivos() {
  const pagina = await listarUsuarios(0)
  return pagina.conteudo.filter((u) => u.ativo && u.perfil === 'VENDEDOR')
}
