import type {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react'
import { CaretDown } from '@phosphor-icons/react'
import type { StatusOS, StatusPagamento } from '../types/dominio'

export function cx(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ')
}

/** Botao com alvo de toque >= 44px: a oficina usa o app de dedo sujo. */
export function Botao({
  variante = 'primario',
  tamanho = 'medio',
  className,
  children,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variante?: 'primario' | 'secundario' | 'perigo' | 'fantasma'
  tamanho?: 'medio' | 'grande'
}) {
  const variantes = {
    /* Branco solido sobre grafite: o unico acento que o monocromatico tem. */
    primario:
      'bg-white text-black shadow-[var(--tk-glow)] hover:bg-tinta-800 active:bg-tinta-700 disabled:bg-tinta-400 disabled:text-tinta-600 disabled:shadow-none',
    secundario:
      'bg-white/[0.04] text-tinta-800 border border-white/12 hover:border-white/25 hover:bg-white/[0.08] active:bg-white/[0.12]',
    /* Vermelho: aviso de acao irreversivel, nunca enfeite. */
    perigo: 'bg-red-600 text-white hover:bg-red-500 active:bg-red-700',
    fantasma: 'bg-transparent text-tinta-600 hover:bg-white/[0.06] hover:text-tinta-900',
  }
  const tamanhos = {
    medio: 'min-h-11 px-5 text-sm',
    grande: 'min-h-13 px-6 text-base',
  }
  return (
    <button
      className={cx(
        'inline-flex items-center justify-center gap-2 rounded-full font-semibold tracking-tight transition-all',
        'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white/85',
        'disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none',
        variantes[variante],
        tamanhos[tamanho],
        className,
      )}
      {...props}
    >
      {children}
    </button>
  )
}

/**
 * Cabecalho padrao das paginas internas.
 * - sobretitulo: rotulo curto em caixa alta (opcional)
 * - acoes: slot a direita para botoes (opcional)
 */
export function CabecalhoPagina({
  sobretitulo,
  titulo,
  descricao,
  acoes,
}: {
  sobretitulo?: string
  titulo: string
  descricao?: string
  acoes?: ReactNode
}) {
  return (
    <header className="mb-5 flex flex-wrap items-end justify-between gap-3 sm:mb-7">
      <div className="min-w-0">
        {sobretitulo && (
          <p className="mb-1.5 text-[11px] font-bold uppercase tracking-[0.2em] text-tinta-500">
            {sobretitulo}
          </p>
        )}
        <h1 className="text-2xl font-bold tracking-tight text-tinta-900 sm:text-[28px]">
          {titulo}
        </h1>
        {descricao && (
          <p className="mt-1.5 max-w-2xl text-sm leading-relaxed text-tinta-600">{descricao}</p>
        )}
      </div>
      {acoes && <div className="flex shrink-0 items-center gap-2">{acoes}</div>}
    </header>
  )
}

export function Campo({
  rotulo,
  erro,
  dica,
  obrigatorio,
  children,
}: {
  rotulo: string
  erro?: string
  dica?: string
  obrigatorio?: boolean
  children: ReactNode
}) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-semibold text-tinta-800">
        {rotulo}
        {obrigatorio && <span className="ml-0.5 text-red-400">*</span>}
      </span>
      {children}
      {dica && !erro && <span className="mt-1 block text-xs text-tinta-500">{dica}</span>}
      {erro && <span className="mt-1 block text-xs font-medium text-red-400">{erro}</span>}
    </label>
  )
}

export function Input({
  erro,
  className,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { erro?: boolean }) {
  return (
    <input
      className={cx(
        'min-h-11 w-full rounded-xl border bg-white/[0.04] px-3.5 text-base text-tinta-900 transition-colors',
        'placeholder:text-tinta-500 focus:outline-2 focus:outline-offset-0 focus:outline-white/70',
        erro ? 'border-red-500/60' : 'border-white/10 hover:border-white/20',
        className,
      )}
      {...props}
    />
  )
}

/**
 * Select com a mesma moldura do Input. O `appearance-none` derruba a setinha
 * do sistema (que vem clara e some no grafite) e desenhamos a nossa.
 */
export function Selecao({
  erro,
  className,
  children,
  ...props
}: SelectHTMLAttributes<HTMLSelectElement> & { erro?: boolean }) {
  return (
    <div className="relative">
      <select
        className={cx(
          'min-h-11 w-full appearance-none rounded-xl border bg-white/[0.04] pr-10 pl-3.5 text-base text-tinta-900 transition-colors',
          'focus:outline-2 focus:outline-offset-0 focus:outline-white/70',
          erro ? 'border-red-500/60' : 'border-white/10 hover:border-white/20',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      <CaretDown
        size={15}
        weight="bold"
        className="pointer-events-none absolute top-1/2 right-3.5 -translate-y-1/2 text-tinta-500"
      />
    </div>
  )
}

export function AreaTexto({
  erro,
  className,
  ...props
}: TextareaHTMLAttributes<HTMLTextAreaElement> & { erro?: boolean }) {
  return (
    <textarea
      className={cx(
        'w-full rounded-xl border bg-white/[0.04] p-3.5 text-base leading-relaxed text-tinta-900 transition-colors',
        'placeholder:text-tinta-500 focus:outline-2 focus:outline-offset-0 focus:outline-white/70',
        erro ? 'border-red-500/60' : 'border-white/10 hover:border-white/20',
        className,
      )}
      {...props}
    />
  )
}

export function Cartao({ className, children }: { className?: string; children: ReactNode }) {
  return <div className={cx('tk-card p-4', className)}>{children}</div>
}

/**
 * Status em preto e branco: a diferenca vem do peso da luz. Branco solido =
 * a OS viva agora; contorno = ainda nao virou trabalho; apagado = encerrada.
 * O texto continua sendo o sinal principal.
 */
const CORES_STATUS: Record<StatusOS, string> = {
  ORCAMENTO: 'border border-dashed border-white/25 text-tinta-600',
  APROVADA: 'border border-white/60 text-tinta-900',
  EM_EXECUCAO: 'bg-white text-black shadow-[var(--tk-glow)]',
  AGUARDANDO_PECA: 'bg-white/[0.08] text-tinta-700 border border-white/15',
  PRONTA: 'bg-tinta-700 text-black',
  ENTREGUE: 'bg-white/[0.05] text-tinta-500 border border-white/8',
  CANCELADA: 'bg-white/[0.04] text-tinta-500 line-through',
}

export const ROTULO_STATUS: Record<StatusOS, string> = {
  ORCAMENTO: 'Orçamento',
  APROVADA: 'Aprovada',
  EM_EXECUCAO: 'Em execução',
  AGUARDANDO_PECA: 'Aguardando peça',
  PRONTA: 'Pronta',
  ENTREGUE: 'Entregue',
  CANCELADA: 'Cancelada',
}

export function EtiquetaStatus({ status }: { status: StatusOS }) {
  return (
    <span
      className={cx(
        'inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold whitespace-nowrap',
        CORES_STATUS[status],
      )}
    >
      {ROTULO_STATUS[status]}
    </span>
  )
}

const CORES_PAGAMENTO: Record<StatusPagamento, string> = {
  PENDENTE: 'border border-dashed border-white/25 text-tinta-600',
  PARCIAL: 'bg-white/[0.08] text-tinta-700 border border-white/15',
  PAGO: 'bg-white text-black',
  ATRASADO: 'border border-red-500/70 text-red-400',
}

export function EtiquetaPagamento({ status }: { status: StatusPagamento }) {
  const rotulos: Record<StatusPagamento, string> = {
    PENDENTE: 'Pendente',
    PARCIAL: 'Parcial',
    PAGO: 'Pago',
    ATRASADO: 'Atrasado',
  }
  return (
    <span
      className={cx(
        'inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold whitespace-nowrap',
        CORES_PAGAMENTO[status],
      )}
    >
      {rotulos[status]}
    </span>
  )
}

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 p-8 text-tinta-500">
      <div className="size-5 animate-spin rounded-full border-2 border-white/15 border-t-white" />
      <span className="text-sm">{texto}</span>
    </div>
  )
}

export function Vazio({
  titulo,
  descricao,
  acao,
}: {
  titulo: string
  descricao?: string
  acao?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 p-10 text-center">
      <p className="font-semibold text-tinta-800">{titulo}</p>
      {descricao && <p className="max-w-sm text-sm text-tinta-500">{descricao}</p>}
      {acao && <div className="mt-3">{acao}</div>}
    </div>
  )
}

export function ErroBox({ mensagem }: { mensagem: string }) {
  return (
    <div className="rounded-xl border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-300">
      {mensagem}
    </div>
  )
}

/** Barra fixa no rodape: a acao principal fica sempre no alcance do polegar. */
export function BarraAcao({ children }: { children: ReactNode }) {
  return (
    <div className="tk-glass sticky bottom-0 z-10 border-t border-white/10 p-3 area-segura-inferior">
      <div className="mx-auto flex max-w-3xl gap-2">{children}</div>
    </div>
  )
}
