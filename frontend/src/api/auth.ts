import { requisitar, sessao } from './http'
import type { LoginResponse } from './tipos'

export async function login(email: string, senha: string) {
  const dados = await requisitar<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, senha }),
  })

  sessao.guardar(dados.token, dados.usuario)
  return dados
}

export function sair() {
  sessao.limpar()
}
