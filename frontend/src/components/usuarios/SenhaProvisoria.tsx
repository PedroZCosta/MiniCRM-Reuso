import { useState } from 'react'
import { Copy, Check } from 'lucide-react'
import Modal from '../Modal'

type Props = {
  senha: string
  aoFechar: () => void
}

export default function SenhaProvisoria({ senha, aoFechar }: Props) {
  const [copiou, setCopiou] = useState(false)

  async function copiar() {
    await navigator.clipboard.writeText(senha)
    setCopiou(true)
  }

  return (
    <Modal
      titulo="Usuário criado"
      aoFechar={aoFechar}
      rodape={
        <button className="btn btn-pri" onClick={aoFechar}>
          Entendi, já anotei
        </button>
      }
    >
      <p className="text-sm text-ink-soft">
        Entregue esta senha à pessoa. Ela não aparece de novo depois que esta janela fechar.
      </p>

      <div className="flex items-center gap-2.5 rounded-[7px] border-2 border-dashed border-accent bg-accent-soft px-4 py-3">
        <code className="flex-1 font-mono text-lg font-semibold tracking-wider text-accent-ink">
          {senha}
        </code>
        <button className="btn btn-sm" onClick={copiar}>
          {copiou ? <Check size={14} /> : <Copy size={14} />}
          {copiou ? 'Copiado' : 'Copiar'}
        </button>
      </div>

      <div className="modal-note">
        No primeiro acesso o sistema obriga a troca. A nova senha precisa ter 8 caracteres, uma
        letra e um número (RN-01).
      </div>
    </Modal>
  )
}
