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

export type StatusCliente = 'PROSPECT' | 'ATIVO' | 'INATIVO'

export type TipoInteracao = 'LIGACAO' | 'EMAIL' | 'REUNIAO'

export type ClienteResponse = {
  idCliente: number
  nome: string
  email: string
  telefone: string | null
  empresa: string | null
  status: StatusCliente
  // ativo=false quando o vendedor foi desativado: o cliente fica sem responsavel ativo.
  vendedor: { idUsuario: number; nome: string; ativo: boolean }
  criadoEm: string
}

export type InteracaoResponse = {
  idInteracao: number
  tipo: TipoInteracao
  dataInteracao: string
  observacoes: string | null
  autor: string
  criadoEm: string
}

// item do historico do cliente: junta interacoes, oportunidades e tarefas.
export type HistoricoItem = {
  tipo: 'INTERACAO' | 'OPORTUNIDADE' | 'TAREFA'
  data: string
  titulo: string
  descricao: string | null
  autor: string
}

export type EtapaOportunidade = 'PROSPECCAO' | 'CONTATO' | 'PROPOSTA' | 'GANHA' | 'PERDIDA'

export type OportunidadeResponse = {
  idOportunidade: number
  titulo: string
  cliente: string
  valorEstimado: number
  vendedor: string
  dataPrevista: string | null
  etapa: EtapaOportunidade
  resultado: EtapaOportunidade | null
  motivoPerda: string | null
}

export type HistoricoEtapaResponse = {
  etapaAnterior: EtapaOportunidade
  etapaNova: EtapaOportunidade
  usuario: string
  alteradoEm: string
}

export type OportunidadeDetalheResponse = {
  oportunidade: OportunidadeResponse
  historico: HistoricoEtapaResponse[]
}

// a ultima coluna vem como FECHADO e junta as ganhas e as perdidas.
export type ColunaFunil = {
  etapa: 'PROSPECCAO' | 'CONTATO' | 'PROPOSTA' | 'FECHADO'
  quantidade: number
  valorTotal: number
  oportunidades: OportunidadeResponse[]
}

export type FunilResponse = {
  colunas: ColunaFunil[]
  valorEmNegociacao: number
}

export type MotivoPerdaResponse = {
  idMotivo: number
  descricao: string
}
