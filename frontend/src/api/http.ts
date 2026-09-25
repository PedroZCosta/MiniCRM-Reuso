import type { ErroResponse } from './tipos'

const BASE = '/api/v1'
const CHAVE_TOKEN = 'crm.token'
const CHAVE_USUARIO = 'crm.usuario'

// erro de negocio vindo da api, ja com o codigo que o backend mandou.
export class ErroApi extends Error {
  status: number
  codigo: string

  constructor(status: number, codigo: string, mensagem: string) {
    super(mensagem)
    this.status = status
    this.codigo = codigo
  }
}

export const sessao = {
  token: () => localStorage.getItem(CHAVE_TOKEN),
  usuario: () => {
    const bruto = localStorage.getItem(CHAVE_USUARIO)
    return bruto ? JSON.parse(bruto) : null
  },
  guardar(token: string, usuario: unknown) {
    localStorage.setItem(CHAVE_TOKEN, token)
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario))
  },
  limpar() {
    localStorage.removeItem(CHAVE_TOKEN)
    localStorage.removeItem(CHAVE_USUARIO)
  },
}

// o unico lugar do front que fala com a api.
export async function requisitar<T>(caminho: string, opcoes: RequestInit = {}): Promise<T> {
  const token = sessao.token()

  const resposta = await fetch(BASE + caminho, {
    ...opcoes,
    headers: {
      'Content-Type': 'application/json',
      // e aqui que o token entra em toda chamada, igual ao JwtAuthFilter espera.
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...opcoes.headers,
    },
  })

  if (!resposta.ok) {
    const erro = (await resposta.json().catch(() => null)) as ErroResponse | null
    throw new ErroApi(
      resposta.status,
      erro?.erro ?? 'ERRO',
      erro?.mensagem ?? 'Não foi possível completar a operação',
    )
  }

  // 204 nao tem corpo nenhum para ler.
  if (resposta.status === 204) return undefined as T
  return resposta.json() as Promise<T>
}
