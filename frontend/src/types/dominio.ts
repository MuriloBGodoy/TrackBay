/** Espelha o dominio do backend (com.trackwheel.domain.model). */

export type Ramo =
  | 'RADIADOR'
  | 'MECANICA_GERAL'
  | 'FUNILARIA_PINTURA'
  | 'ELETRICA'
  | 'SUSPENSAO_FREIOS'
  | 'AR_CONDICIONADO'
  | 'TROCA_OLEO'
  | 'PNEUS_ALINHAMENTO'
  | 'INJECAO_ELETRONICA'
  | 'OUTRO'

export type Papel = 'OWNER' | 'MANAGER' | 'ATTENDANT' | 'MECHANIC'

export type TipoPessoa = 'FISICA' | 'JURIDICA'

export type TipoVeiculo = 'CARRO' | 'MOTO' | 'CAMINHAO' | 'VAN' | 'MAQUINA'

export type Combustivel = 'GASOLINA' | 'ETANOL' | 'FLEX' | 'DIESEL' | 'GNV' | 'ELETRICO' | 'HIBRIDO'

export type Cambio = 'MANUAL' | 'AUTOMATICO' | 'AUTOMATIZADO' | 'CVT'

export type StatusOS =
  | 'ORCAMENTO'
  | 'APROVADA'
  | 'EM_EXECUCAO'
  | 'AGUARDANDO_PECA'
  | 'PRONTA'
  | 'ENTREGUE'
  | 'CANCELADA'

export type FormaPagamento =
  | 'DINHEIRO'
  | 'PIX'
  | 'DEBITO'
  | 'CREDITO'
  | 'BOLETO'
  | 'TRANSFERENCIA'
  | 'FATURADO'
  | 'CHEQUE'

export type StatusPagamento = 'PENDENTE' | 'PARCIAL' | 'PAGO' | 'ATRASADO'

export type TipoCampo =
  | 'TEXTO'
  | 'TEXTO_LONGO'
  | 'NUMERO'
  | 'DECIMAL'
  | 'MOEDA'
  | 'DATA'
  | 'BOOLEANO'
  | 'SELECT'
  | 'MULTI_SELECT'
  | 'FOTO'
  | 'ASSINATURA'
  | 'CHECKLIST'

export type OrigemPeca = 'ESTOQUE_PROPRIO' | 'COMPRADA' | 'FORNECIDA_CLIENTE'

export type TipoMovimentacao = 'ENTRADA' | 'SAIDA' | 'AJUSTE' | 'DEVOLUCAO'

export interface Endereco {
  cep?: string
  logradouro?: string
  numero?: string
  complemento?: string
  bairro?: string
  cidade?: string
  uf?: string
}

export interface Usuario {
  id: string
  uid?: string
  oficinaId?: string
  nome?: string
  email?: string
  fotoUrl?: string
  papel: Papel
  ativo: boolean
}

export interface Oficina {
  id: string
  nomeFantasia: string
  razaoSocial?: string
  cnpj?: string
  inscricaoEstadual?: string
  ramos: Ramo[]
  endereco?: Endereco
  telefone?: string
  whatsapp?: string
  email?: string
  logoUrl?: string
  horarioFuncionamento?: string
  config?: {
    prefixoNumeroOS?: string
    proximoNumeroOS?: number
    textoGarantiaPadrao?: string
    garantiaPadraoDias?: number
  }
}

export interface Sessao {
  usuario: Usuario
  oficina: Oficina | null
  precisaOnboarding: boolean
}

export interface Cliente {
  id: string
  oficinaId?: string
  tipoPessoa: TipoPessoa
  nome: string
  telefone?: string
  whatsapp?: string
  email?: string
  endereco?: Endereco
  observacoes?: string
  ativo: boolean
  consentimentoLgpd?: boolean
  dadosPF?: {
    cpf?: string
    rg?: string
    dataNascimento?: string
    cnhNumero?: string
    cnhCategoria?: string
    cnhValidade?: string
  }
  dadosPJ?: {
    cnpj?: string
    razaoSocial?: string
    inscricaoEstadual?: string
    inscricaoMunicipal?: string
    contatoResponsavel?: { nome?: string; cargo?: string; telefone?: string }
    condicoesComerciais?: {
      prazoPagamentoDias?: number
      limiteCredito?: number
      tabelaPreco?: string
      faturamentoMensal?: boolean
    }
  }
}

export interface Veiculo {
  id: string
  oficinaId?: string
  clienteId: string
  placa: string
  placaFormatada?: string
  marca?: string
  modelo?: string
  versao?: string
  anoFabricacao?: number
  anoModelo?: number
  cor?: string
  chassi?: string
  renavam?: string
  combustivel?: Combustivel
  cambio?: Cambio
  motorizacao?: string
  kmAtual?: number
  tipoVeiculo: TipoVeiculo
  fotos?: string[]
  observacoes?: string
  avariasPreExistentes?: string
  descricaoCurta?: string
  ativo: boolean
}

export interface CampoDinamico {
  chave: string
  rotulo: string
  tipo: TipoCampo
  obrigatorio: boolean
  opcoes: string[]
  ordem: number
  grupo?: string
  condicional?: { campo: string; valor: unknown }
  aplicavelA?: TipoVeiculo[]
  placeholder?: string
  unidade?: string
}

export interface TemplateCampos {
  id: string
  oficinaId: string
  ramo: Ramo
  versao: number
  campos: CampoDinamico[]
  ativo: boolean
}

export interface ItemServico {
  id?: string
  descricao: string
  tipo?: string
  valorUnitario: number
  quantidade: number
  desconto?: number
  mecanicoId?: string
  mecanicoNome?: string
  total?: number
}

export interface ItemPeca {
  id?: string
  produtoId?: string
  descricao: string
  codigo?: string
  quantidade: number
  valorUnitario: number
  desconto?: number
  origem: OrigemPeca
  total?: number
}

export interface Parcela {
  id?: string
  forma: FormaPagamento
  valor: number
  numeroParcelas?: number
  vencimento?: string
  recebidoEm?: string
  valorRecebido?: number
  taxaMaquininha?: number
}

export interface Pagamento {
  parcelas: Parcela[]
  status: StatusPagamento
  totalPrevisto?: number
  totalRecebido?: number
  saldo?: number
}

export interface TransicaoStatus {
  de: StatusOS
  para: StatusOS
  autorId?: string
  autorNome?: string
  em: string
  observacao?: string
}

export interface OrdemServico {
  id: string
  oficinaId?: string
  numero: string
  clienteId: string
  clienteNome?: string
  veiculoId: string
  veiculoPlaca?: string
  veiculoDescricao?: string
  kmEntrada?: number
  status: StatusOS
  historicoStatus?: TransicaoStatus[]
  dataAbertura: string
  previsaoEntrega?: string
  dataConclusao?: string
  dataEntrega?: string
  reclamacaoCliente?: string
  diagnosticoTecnico?: string
  ramo: Ramo
  schemaVersion: number
  camposDinamicos: Record<string, unknown>
  itensServico: ItemServico[]
  itensPeca: ItemPeca[]
  descontoGeral?: number
  acrescimo?: number
  pagamento: Pagamento
  garantiaDias?: number
  garantiaKm?: number
  textoGarantia?: string
  assinaturaClienteUrl?: string
  checklistEntrada?: {
    nivelCombustivel?: string
    estadoPneus?: string
    avarias?: string
    itensNoVeiculo?: string[]
    fotos?: string[]
  }
  subtotalServicos?: number
  subtotalPecas?: number
  total?: number
}

export interface Produto {
  id: string
  codigoInterno?: string
  codigoBarras?: string
  nome: string
  descricao?: string
  marca?: string
  categoria?: string
  unidade?: string
  precoCusto?: number
  precoVenda: number
  margem?: number
  estoqueAtual?: number
  estoqueMinimo?: number
  localizacao?: string
  fornecedorId?: string
  aplicacao?: string[]
  fotoUrl?: string
  ncm?: string
  estoqueBaixo?: boolean
  ativo: boolean
}

export interface MovimentacaoEstoque {
  id: string
  produtoId: string
  produtoNome?: string
  tipo: TipoMovimentacao
  quantidade: number
  saldoAnterior: number
  saldoPosterior: number
  motivo?: string
  autorNome?: string
  em: string
}

export interface VendaBalcao {
  id: string
  numero?: string
  clienteId?: string
  clienteNome?: string
  itens: ItemPeca[]
  descontoGeral?: number
  pagamento: Pagamento
  vendedorNome?: string
  dataVenda: string
  cancelada?: boolean
  subtotal?: number
  total?: number
}

export interface Agendamento {
  id: string
  clienteId?: string
  clienteNome?: string
  veiculoId?: string
  veiculoPlaca?: string
  inicio: string
  duracaoEstimadaMinutos: number
  servicoPrevisto?: string
  observacoes?: string
  mecanicoId?: string
  status: 'AGENDADO' | 'CONFIRMADO' | 'EM_ATENDIMENTO' | 'CONCLUIDO' | 'NAO_COMPARECEU' | 'CANCELADO'
  ordemServicoId?: string
}

export interface ResumoDashboard {
  faturamentoDia: number
  faturamentoMes: number
  osAbertas: number
  osAguardandoPeca: number
  osProntas: number
  ticketMedio: number
  produtosEstoqueBaixo: number
  alertasEstoque: {
    produtoId: string
    nome: string
    estoqueAtual: number
    estoqueMinimo: number
  }[]
}

/** Problem Details (RFC 7807) devolvido pelo backend. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: Record<string, string>
}
