// cores fixas para a mesma pessoa cair sempre na mesma cor.
const cores = ['#2a78d6', '#2f8f5b', '#b05a2a', '#7a5ea8', '#c0392b', '#6b6b63']

function iniciais(nome: string) {
  const partes = nome.trim().split(' ')
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase()
  return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase()
}

function corDoNome(nome: string) {
  // soma os codigos das letras para escolher a cor sempre igual.
  const soma = [...nome].reduce((total, letra) => total + letra.charCodeAt(0), 0)
  return cores[soma % cores.length]
}

export default function Avatar({ nome }: { nome: string }) {
  return (
    <div className="avatar" style={{ background: corDoNome(nome) }}>
      {iniciais(nome)}
    </div>
  )
}
