import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { login } from '../api/auth'
import { ErroApi } from '../api/http'

export default function Login() {
  // cada campo do formulario vive num estado.
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const navegar = useNavigate()

  // o async aqui porque a api demora a responder e nao da para travar a tela.
  async function entrar(evento: React.FormEvent) {
    // sem isso o navegador recarrega a pagina inteira ao enviar o formulario.
    evento.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      const { usuario } = await login(email, senha)

      // primeiro acesso: a senha e provisoria e precisa ser trocada.
      navegar(usuario.trocarSenha ? '/trocar-senha' : '/usuarios', { replace: true })
    } catch (e) {
      // o backend ja manda a mensagem pronta, entao so mostramos ela.
      setErro(e instanceof ErroApi ? e.message : 'Servidor indisponível')
    } finally {
      // o botao volta ao normal tendo dado certo ou nao.
      setEnviando(false)
    }
  }

  return (
    <div className="grid min-h-screen grid-cols-1 md:grid-cols-2">
      {/* lado esquerdo: a marca e o recado do projeto. */}
      <div className="relative flex flex-col justify-between overflow-hidden border-r-2 border-line bg-surface-alt p-12">
        {/* o quadriculado de fundo, que some nas bordas. */}
        <div
          className="pointer-events-none absolute inset-0 opacity-60"
          style={{
            backgroundImage:
              'linear-gradient(var(--color-line-soft) 1.5px, transparent 1.5px), linear-gradient(90deg, var(--color-line-soft) 1.5px, transparent 1.5px)',
            backgroundSize: '34px 34px',
            maskImage: 'radial-gradient(circle at 25% 25%, #000 0%, transparent 65%)',
            WebkitMaskImage: 'radial-gradient(circle at 25% 25%, #000 0%, transparent 65%)',
          }}
        />

        <div className="relative z-10 flex items-center gap-[9px] font-hand text-[26px]">
          <span className="h-3 w-3 rounded-[3px] border-2 border-line bg-accent" />
          miniCRM
        </div>

        <div className="relative z-10 max-w-[420px]">
          <h2 className="text-[32px] leading-tight text-ink">
            Um núcleo de código. Quatro módulos. Nenhuma regra duplicada.
          </h2>
          <p className="mt-3.5 text-[14.5px] leading-relaxed text-ink-soft">
            Autenticação, clientes, funil e tarefas dividem as mesmas permissões, o mesmo
            tratamento de erro e o mesmo escopo de carteira. Quem entra decide o que a tela mostra.
          </p>
        </div>

        <div className="relative z-10 mt-9 flex gap-6">
          {[
            { valor: '4', rotulo: 'Módulos' },
            { valor: '3', rotulo: 'Perfis' },
            { valor: '25', rotulo: 'Permissões' },
          ].map((item) => (
            <div
              key={item.rotulo}
              className="rounded-[7px] border-2 border-line bg-surface px-4 py-2.5 shadow-card-sm"
            >
              <b className="block font-hand text-2xl font-normal text-ink">{item.valor}</b>
              <span className="text-[11px] tracking-wide text-muted uppercase">{item.rotulo}</span>
            </div>
          ))}
        </div>
      </div>

      {/* lado direito: o formulario. */}
      <div className="flex items-center justify-center p-8">
        <div className="w-full max-w-[370px]">
          <h1 className="mb-1.5 text-[27px]">Entrar no sistema</h1>
          <p className="mb-6 text-[13.5px] text-muted">
            Use o e-mail e a senha cadastrados pelo administrador.
          </p>

          <form onSubmit={entrar} className="flex flex-col gap-4" noValidate>
            <div className="field">
              <label htmlFor="email">E-mail</label>
              <input
                id="email"
                className="input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="voce@email.com"
              />
            </div>

            <div className="field">
              <label htmlFor="senha">Senha</label>
              <input
                id="senha"
                className="input"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="••••••••"
              />
            </div>

            {/* so aparece quando existe erro. */}
            {erro && (
              <div className="rounded-[5px] border-2 border-dashed border-red bg-red-soft px-3 py-2 text-xs text-red">
                {erro}
              </div>
            )}

            <button
              type="submit"
              disabled={enviando}
              className="btn btn-pri btn-block mt-1 h-[42px] text-sm"
            >
              {enviando ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          <div className="mt-4 flex items-center justify-between">
            <Link to="/recuperar-senha" className="font-hand text-[15px] text-accent">
              Esqueci minha senha
            </Link>
          </div>

          <div className="mt-5 rounded-[5px] border-[1.5px] border-dashed border-accent bg-accent-soft px-3.5 py-3 text-xs leading-relaxed text-accent-ink">
            Cinco tentativas erradas bloqueiam a conta por 15 minutos (RN-03). O primeiro acesso
            exige troca de senha.
          </div>
        </div>
      </div>
    </div>
  )
}
