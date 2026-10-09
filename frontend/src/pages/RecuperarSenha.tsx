import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { ArrowLeft } from 'lucide-react'
import { recuperarSenha, redefinirSenha } from '../api/auth'
import { ErroApi } from '../api/http'

export default function RecuperarSenha() {
  // a tela tem dois passos: pedir o codigo e usar o codigo.
  const [passo, setPasso] = useState<'pedir' | 'redefinir'>('pedir')
  const [email, setEmail] = useState('')
  const [codigo, setCodigo] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const navegar = useNavigate()

  async function pedirCodigo(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await recuperarSenha(email)
      setPasso('redefinir')
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    } finally {
      setEnviando(false)
    }
  }

  async function confirmar(evento: React.FormEvent) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await redefinirSenha(email, codigo, novaSenha)
      navegar('/login', { replace: true })
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-8">
      <div className="w-full max-w-[400px]">
        <Link to="/login" className="mb-3 inline-flex items-center gap-1.5 font-hand text-base text-muted hover:text-ink">
          <ArrowLeft size={15} />
          Voltar para o login
        </Link>

        <div className="card p-8">
          {passo === 'pedir' ? (
            <>
              <h1 className="mb-1.5 text-[25px]">Recuperar senha</h1>
              <p className="mb-6 text-[13.5px] text-muted">
                Informe o e-mail da conta. Enviamos um código de 6 dígitos válido por 1 hora.
              </p>

              <form onSubmit={pedirCodigo} className="flex flex-col gap-4" noValidate>
                <div className="field">
                  <label htmlFor="email-recuperar">E-mail</label>
                  <input
                    id="email-recuperar"
                    className="input"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="voce@email.com"
                    autoFocus
                  />
                </div>

                {erro && (
                  <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
                    {erro}
                  </div>
                )}

                <button type="submit" disabled={enviando} className="btn btn-pri btn-block h-[42px]">
                  {enviando ? 'Enviando...' : 'Enviar código'}
                </button>
              </form>
            </>
          ) : (
            <>
              <h1 className="mb-1.5 text-[25px]">Digite o código</h1>
              <p className="mb-6 text-[13.5px] text-muted">
                Se existe conta com <b>{email}</b>, o código foi enviado. Ele vale por 1 hora e só
                pode ser usado uma vez.
              </p>

              <form onSubmit={confirmar} className="flex flex-col gap-4" noValidate>
                <div className="field">
                  <label htmlFor="codigo">Código de 6 dígitos</label>
                  <input
                    id="codigo"
                    className="input text-center font-mono text-lg tracking-[0.4em]"
                    value={codigo}
                    onChange={(e) => setCodigo(e.target.value)}
                    maxLength={6}
                    placeholder="000000"
                    autoFocus
                  />
                </div>

                <div className="field">
                  <label htmlFor="nova-senha">Nova senha</label>
                  <input
                    id="nova-senha"
                    className="input"
                    type="password"
                    value={novaSenha}
                    onChange={(e) => setNovaSenha(e.target.value)}
                  />
                  <span className="text-[11.5px] text-muted">
                    Mínimo de 8 caracteres, com ao menos uma letra e um número.
                  </span>
                </div>

                {erro && (
                  <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
                    {erro}
                  </div>
                )}

                <button type="submit" disabled={enviando} className="btn btn-pri btn-block h-[42px]">
                  {enviando ? 'Salvando...' : 'Redefinir senha'}
                </button>
              </form>

              <div className="modal-note mt-4">
                Enquanto o envio de e-mail real não existe, o código aparece no log do backend:
                procure por <code>[email] codigo de recuperacao</code>.
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
