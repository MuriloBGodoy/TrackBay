/**
 * Nome humano de cada enum do dominio.
 *
 * O backend manda o enum cru (`MECANICA_GERAL`, `FORNECIDA_CLIENTE`) porque e
 * ele que carrega significado na API. Na tela, quem le e o pessoal do balcao —
 * entao a traducao vive aqui, num lugar so, com acento e tudo.
 */

import type {
  Cambio,
  Combustivel,
  FormaPagamento,
  LocalOficina,
  OrigemPeca,
  Papel,
  Ramo,
  TipoMovimentacao,
  TipoObjeto,
  TipoPessoa,
  TipoVeiculo,
} from '../types/dominio'

export const ROTULO_RAMO: Record<Ramo, string> = {
  MECANICA_GERAL: 'Mecânica geral',
  SUSPENSAO_FREIOS: 'Suspensão e freios',
  TROCA_OLEO: 'Troca de óleo',
  INJECAO_ELETRONICA: 'Injeção eletrônica',
  ELETRICA: 'Elétrica',
  AR_CONDICIONADO: 'Ar-condicionado',
  PNEUS_ALINHAMENTO: 'Pneus e alinhamento',
  FUNILARIA_PINTURA: 'Funilaria e pintura',
  RADIADOR: 'Radiador',
  OUTRO: 'Outro',
}

export const ROTULO_PAPEL: Record<Papel, string> = {
  OWNER: 'Dono',
  MANAGER: 'Gerente',
  ATTENDANT: 'Atendente',
  MECHANIC: 'Mecânico',
}

export const ROTULO_FORMA_PAGAMENTO: Record<FormaPagamento, string> = {
  DINHEIRO: 'Dinheiro',
  PIX: 'PIX',
  DEBITO: 'Débito',
  CREDITO: 'Crédito',
  BOLETO: 'Boleto',
  TRANSFERENCIA: 'Transferência',
  FATURADO: 'Faturado',
  CHEQUE: 'Cheque',
}

export const ROTULO_LOCAL: Record<LocalOficina, string> = {
  PATIO: 'Pátio',
  BOX: 'Box',
  ELEVADOR: 'Elevador',
  DUCHA: 'Ducha',
  TESTE: 'Em teste',
  EXTERNO: 'Em terceiro',
  OUTRO: 'Outro',
}

/**
 * Onde o objeto está, em texto. Com OUTRO, quem manda é o nome que a oficina
 * deu ao canto — é para isso que o campo livre existe.
 */
export function localRotulo(local?: LocalOficina, detalhe?: string): string {
  if (!local) return '—'
  return local === 'OUTRO' && detalhe ? detalhe : ROTULO_LOCAL[local]
}

export const ROTULO_TIPO_OBJETO: Record<TipoObjeto, string> = {
  VEICULO: 'Veículo',
  PECA: 'Peça avulsa',
}

export const ROTULO_TIPO_VEICULO: Record<TipoVeiculo, string> = {
  CARRO: 'Carro',
  MOTO: 'Moto',
  CAMINHAO: 'Caminhão',
  VAN: 'Van',
  MAQUINA: 'Máquina',
}

export const ROTULO_COMBUSTIVEL: Record<Combustivel, string> = {
  GASOLINA: 'Gasolina',
  ETANOL: 'Etanol',
  FLEX: 'Flex',
  DIESEL: 'Diesel',
  GNV: 'GNV',
  ELETRICO: 'Elétrico',
  HIBRIDO: 'Híbrido',
}

export const ROTULO_CAMBIO: Record<Cambio, string> = {
  MANUAL: 'Manual',
  AUTOMATICO: 'Automático',
  AUTOMATIZADO: 'Automatizado',
  CVT: 'CVT',
}

export const ROTULO_MOVIMENTACAO: Record<TipoMovimentacao, string> = {
  ENTRADA: 'Entrada',
  SAIDA: 'Saída',
  AJUSTE: 'Ajuste',
  DEVOLUCAO: 'Devolução',
}

export const ROTULO_ORIGEM_PECA: Record<OrigemPeca, string> = {
  ESTOQUE_PROPRIO: 'Estoque próprio',
  COMPRADA: 'Comprada',
  FORNECIDA_CLIENTE: 'Fornecida pelo cliente',
}

export const ROTULO_TIPO_PESSOA: Record<TipoPessoa, string> = {
  FISICA: 'Pessoa física',
  JURIDICA: 'Pessoa jurídica',
}

export const ROTULO_AGENDAMENTO: Record<string, string> = {
  AGENDADO: 'Agendado',
  CONFIRMADO: 'Confirmado',
  EM_ATENDIMENTO: 'Em atendimento',
  CONCLUIDO: 'Concluído',
  NAO_COMPARECEU: 'Não compareceu',
  CANCELADO: 'Cancelado',
}
