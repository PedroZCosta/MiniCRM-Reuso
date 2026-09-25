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

export function trocarSenha(senhaAtual: string, novaSenha: string) {
  return requisitar<void>('/auth/trocar-senha', {
    method: 'PUT',
    body: JSON.stringify({ senhaAtual, novaSenha }),
  })
}

// responde 204 mesmo se o e-mail nao existir, para nao entregar quem tem conta.
export function recuperarSenha(email: string) {
  return requisitar<void>('/auth/recuperar-senha', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
}

export function redefinirSenha(email: string, codigo: string, novaSenha: string) {
  return requisitar<void>('/auth/redefinir-senha', {
    method: 'POST',
    body: JSON.stringify({ email, codigo, novaSenha }),
  })
}

export function sair() {
  sessao.limpar()
}
