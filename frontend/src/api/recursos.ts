import { api } from './client'
import type {
  Agendamento,
  Cliente,
  MovimentacaoEstoque,
  OrdemServico,
  Oficina,
  Produto,
  Ramo,
  ResumoDashboard,
  Sessao,
  StatusOS,
  TemplateCampos,
  Usuario,
  Veiculo,
  VendaBalcao,
} from '../types/dominio'

export const sessaoApi = {
  me: () => api.get<Sessao>('/oficina/me').then((r) => r.data),
  onboarding: (oficina: Partial<Oficina>) =>
    api.post<Oficina>('/oficina/onboarding', oficina).then((r) => r.data),
  oficina: () => api.get<Oficina>('/oficina').then((r) => r.data),
  atualizarOficina: (oficina: Partial<Oficina>) =>
    api.put<Oficina>('/oficina', oficina).then((r) => r.data),
  usuarios: () => api.get<Usuario[]>('/oficina/usuarios').then((r) => r.data),
}

export const clientesApi = {
  listar: (busca?: string) =>
    api.get<Cliente[]>('/clientes', { params: busca ? { busca } : {} }).then((r) => r.data),
  porId: (id: string) => api.get<Cliente>(`/clientes/${id}`).then((r) => r.data),
  criar: (cliente: Partial<Cliente>) => api.post<Cliente>('/clientes', cliente).then((r) => r.data),
  atualizar: (id: string, cliente: Partial<Cliente>) =>
    api.put<Cliente>(`/clientes/${id}`, cliente).then((r) => r.data),
  remover: (id: string) => api.delete(`/clientes/${id}`).then(() => undefined),
}

export const veiculosApi = {
  listar: (params?: { busca?: string; clienteId?: string }) =>
    api.get<Veiculo[]>('/veiculos', { params }).then((r) => r.data),
  porId: (id: string) => api.get<Veiculo>(`/veiculos/${id}`).then((r) => r.data),
  porPlaca: (placa: string) => api.get<Veiculo>(`/veiculos/placa/${placa}`).then((r) => r.data),
  historico: (id: string) => api.get<OrdemServico[]>(`/veiculos/${id}/historico`).then((r) => r.data),
  criar: (veiculo: Partial<Veiculo>) => api.post<Veiculo>('/veiculos', veiculo).then((r) => r.data),
  atualizar: (id: string, veiculo: Partial<Veiculo>) =>
    api.put<Veiculo>(`/veiculos/${id}`, veiculo).then((r) => r.data),
  remover: (id: string) => api.delete(`/veiculos/${id}`).then(() => undefined),
}

export const ordensApi = {
  listar: (status?: StatusOS) =>
    api.get<OrdemServico[]>('/ordens', { params: status ? { status } : {} }).then((r) => r.data),
  porId: (id: string) => api.get<OrdemServico>(`/ordens/${id}`).then((r) => r.data),
  criar: (os: Partial<OrdemServico>) => api.post<OrdemServico>('/ordens', os).then((r) => r.data),
  atualizar: (id: string, os: Partial<OrdemServico>) =>
    api.put<OrdemServico>(`/ordens/${id}`, os).then((r) => r.data),
  mudarStatus: (id: string, status: StatusOS, observacao?: string) =>
    api.post<OrdemServico>(`/ordens/${id}/status`, { status, observacao }).then((r) => r.data),
  aprovar: (id: string, assinaturaUrl?: string) =>
    api.post<OrdemServico>(`/ordens/${id}/aprovar`, { assinaturaUrl }).then((r) => r.data),
  transicoes: (id: string) =>
    api
      .get<{ atual: StatusOS; permitidos: StatusOS[] }>(`/ordens/${id}/transicoes`)
      .then((r) => r.data),
  pdf: (id: string) =>
    api.get<Blob>(`/ordens/${id}/pdf`, { responseType: 'blob' }).then((r) => r.data),
}

export const templatesApi = {
  ramos: () => api.get<{ valor: Ramo; rotulo: string }[]>('/templates/ramos').then((r) => r.data),
  listar: () => api.get<TemplateCampos[]>('/templates').then((r) => r.data),
  porRamo: (ramo: Ramo, versao?: number) =>
    api
      .get<TemplateCampos>(`/templates/${ramo}`, { params: versao ? { versao } : {} })
      .then((r) => r.data),
  salvar: (ramo: Ramo, campos: TemplateCampos['campos']) =>
    api.put<TemplateCampos>(`/templates/${ramo}`, campos).then((r) => r.data),
}

export const estoqueApi = {
  produtos: (busca?: string) =>
    api.get<Produto[]>('/estoque/produtos', { params: busca ? { busca } : {} }).then((r) => r.data),
  produto: (id: string) => api.get<Produto>(`/estoque/produtos/${id}`).then((r) => r.data),
  porCodigoBarras: (codigo: string) =>
    api.get<Produto>(`/estoque/produtos/barras/${codigo}`).then((r) => r.data),
  criar: (produto: Partial<Produto>) =>
    api.post<Produto>('/estoque/produtos', produto).then((r) => r.data),
  atualizar: (id: string, produto: Partial<Produto>) =>
    api.put<Produto>(`/estoque/produtos/${id}`, produto).then((r) => r.data),
  movimentar: (dados: {
    produtoId: string
    tipo: MovimentacaoEstoque['tipo']
    quantidade: number
    motivo?: string
  }) => api.post<MovimentacaoEstoque>('/estoque/movimentacoes', dados).then((r) => r.data),
  movimentacoes: (produtoId?: string) =>
    api
      .get<MovimentacaoEstoque[]>('/estoque/movimentacoes', { params: produtoId ? { produtoId } : {} })
      .then((r) => r.data),
  alertas: () => api.get<Produto[]>('/estoque/alertas').then((r) => r.data),
}

export const vendasApi = {
  listar: () => api.get<VendaBalcao[]>('/vendas').then((r) => r.data),
  registrar: (venda: Partial<VendaBalcao>) =>
    api.post<VendaBalcao>('/vendas', venda).then((r) => r.data),
  cancelar: (id: string) => api.post<VendaBalcao>(`/vendas/${id}/cancelar`).then((r) => r.data),
}

export const agendaApi = {
  listar: (params?: { dia?: string; de?: string; ate?: string }) =>
    api.get<Agendamento[]>('/agenda', { params }).then((r) => r.data),
  criar: (agendamento: Partial<Agendamento>) =>
    api.post<Agendamento>('/agenda', agendamento).then((r) => r.data),
  atualizar: (id: string, agendamento: Partial<Agendamento>) =>
    api.put<Agendamento>(`/agenda/${id}`, agendamento).then((r) => r.data),
  remover: (id: string) => api.delete(`/agenda/${id}`).then(() => undefined),
}

export const dashboardApi = {
  resumo: () => api.get<ResumoDashboard>('/dashboard').then((r) => r.data),
}

export const arquivosApi = {
  /** Sobe a imagem e devolve a URL para guardar no campo da OS. */
  enviar: (arquivo: File, pasta = 'ordens') => {
    const corpo = new FormData()
    corpo.append('arquivo', arquivo)
    return api
      .post<{ caminho: string; url: string }>('/arquivos', corpo, {
        params: { pasta },
        // Deixa o browser definir o boundary do multipart.
        headers: { 'Content-Type': undefined },
      })
      .then((r) => r.data)
  },
}
