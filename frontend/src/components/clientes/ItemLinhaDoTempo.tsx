import type { LucideIcon } from 'lucide-react'
import { formatarDataHora } from './formato'

type Props = {
  Icone: LucideIcon
  titulo: string
  data: string
  texto: string | null
  autor: string
}

export default function ItemLinhaDoTempo({ Icone, titulo, data, texto, autor }: Props) {
  return (
    <div className="tl-item">
      <div className="tl-ico">
        <Icone size={16} />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex justify-between gap-2.5 font-bold">
          <span>{titulo}</span>
          <time className="text-xs font-medium text-muted">{formatarDataHora(data)}</time>
        </div>
        {texto && <div className="mt-1 text-[13px] whitespace-pre-line text-ink-soft">{texto}</div>}
        <div className="mt-1.5 text-[11.5px] text-muted">por {autor}</div>
      </div>
    </div>
  )
}
