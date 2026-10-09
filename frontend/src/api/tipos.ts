// espelho dos records do backend. se um campo mudar la, muda aqui.

export type PerfilUsuario = 'ADMIN' | 'GERENTE' | 'VENDEDOR'

export type UsuarioResponse = {
  idUsuario: number
  nome: string
  email: string
  perfil: PerfilUsuario
  ativo: boolean
  trocarSenha: boolean
}

export type LoginResponse = {
  token: string
  expiraEm: string
  usuario: UsuarioResponse
}

// envelope de toda listagem paginada.
export type PageResponse<T> = {
  conteudo: T[]
  pagina: number
  tamanho: number
  totalPaginas: number
  totalRegistros: number
}

// formato que o ApiExceptionHandler devolve quando algo da errado.
export type ErroResponse = {
  status: number
  erro: string
  mensagem: string
  detalhes?: string[]
}
