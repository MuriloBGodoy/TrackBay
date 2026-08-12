import axios, { AxiosError } from 'axios'
import type { ProblemDetail } from '../types/dominio'
import { obterTokenAtual, MODO_DEV, USUARIO_DEV } from '../auth/firebase'

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

/**
 * Injeta o ID Token em toda chamada. No modo dev (sem credencial do Firebase)
 * manda o header X-Dev-User, que o backend resolve para o usuario semeado.
 */
api.interceptors.request.use(async (config) => {
  if (MODO_DEV) {
    config.headers.set('X-Dev-User', USUARIO_DEV)
    return config
  }
  const token = await obterTokenAtual()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

/** Erro de API ja traduzido a partir do Problem Details do backend. */
export class ErroApi extends Error {
  status: number
  problema?: ProblemDetail

  constructor(mensagem: string, status: number, problema?: ProblemDetail) {
    super(mensagem)
    this.name = 'ErroApi'
    this.status = status
    this.problema = problema
  }

  /** Erros por campo, quando a resposta veio de uma validacao de formulario. */
  get errosDeCampo(): Record<string, string> | undefined {
    return this.problema?.errors
  }
}

api.interceptors.response.use(
  (resposta) => resposta,
  (erro: AxiosError<ProblemDetail>) => {
    if (!erro.response) {
      return Promise.reject(
        new ErroApi('Sem conexao com o servidor. Verifique sua internet.', 0),
      )
    }
    const { status, data } = erro.response
    const mensagem =
      data?.detail ||
      data?.title ||
      (status === 401
        ? 'Sessao expirada. Entre novamente.'
        : status === 403
          ? 'Voce nao tem permissao para esta acao.'
          : 'Erro inesperado. Tente novamente.')
    return Promise.reject(new ErroApi(mensagem, status, data))
  },
)
