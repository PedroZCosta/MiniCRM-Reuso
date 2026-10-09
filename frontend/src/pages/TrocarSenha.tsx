import { useState } from 'react'
import { useNavigate } from 'react-router'
import { trocarSenha } from '../api/auth'
import { ErroApi, sessao } from '../api/http'
import type { UsuarioResponse } from '../api/tipos'

export default function TrocarSenha() {
  const [senhaAtual, setSenhaAtual] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const navegar = useNavigate()

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')

    // essa conferencia e so da tela; o backend nem recebe a confirmacao.
    if (novaSenha !== confirmacao) {
      setErro('A confirmação não bate com a nova senha')
      return
    }

    setEnviando(true)
    try {
      await trocarSenha(senhaAtual, novaSenha)

      // o usuario guardado ainda diz trocarSenha=true, entao atualizamos.
      const usuario = sessao.usuario() as UsuarioResponse
      sessao.guardar(sessao.token()!, { ...usuario, trocarSenha: false })

      navegar('/usuarios', { replace: true })
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-8">
      <div className="card w-full max-w-[420px] p-8">
        <h1 className="mb-1.5 text-[25px]">Trocar senha</h1>
        <p className="mb-6 text-[13.5px] text-muted">
          Você entrou com uma senha provisória. Defina a sua antes de continuar.
        </p>

        <form onSubmit={enviar} className="flex flex-col gap-4" noValidate>
          <div className="field">
            <label htmlFor="atual">Senha atual</label>
            <input
              id="atual"
              className="input"
              type="password"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="nova">Nova senha</label>
            <input
              id="nova"
              className="input"
              type="password"
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
            />
            <span className="text-[11.5px] text-muted">
              Mínimo de 8 caracteres, com ao menos uma letra e um número.
            </span>
          </div>

          <div className="field">
            <label htmlFor="confirmacao">Repita a nova senha</label>
            <input
              id="confirmacao"
              className="input"
              type="password"
              value={confirmacao}
              onChange={(e) => setConfirmacao(e.target.value)}
            />
          </div>

          {erro && (
            <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
              {erro}
            </div>
          )}

          <button type="submit" disabled={enviando} className="btn btn-pri btn-block h-[42px]">
            {enviando ? 'Salvando...' : 'Salvar nova senha'}
          </button>
        </form>
      </div>
    </div>
  )
}
