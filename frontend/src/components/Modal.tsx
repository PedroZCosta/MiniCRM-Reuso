import { X } from 'lucide-react'

type ModalProps = {
  titulo: string
  aoFechar: () => void
  children: React.ReactNode
  rodape?: React.ReactNode
}

export default function Modal({ titulo, aoFechar, children, rodape }: ModalProps) {
  return (
    // clicar no fundo escuro fecha.
    <div className="overlay" onClick={aoFechar}>
      {/* aqui dentro o clique para de subir, senao fechava ao clicar no proprio modal. */}
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{titulo}</h3>
          <button className="modal-x" onClick={aoFechar} title="Fechar">
            <X size={16} />
          </button>
        </div>

        <div className="modal-body">{children}</div>

        {rodape && <div className="modal-foot">{rodape}</div>}
      </div>
    </div>
  )
}
