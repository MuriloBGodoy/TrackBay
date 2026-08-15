import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { MapPin } from '@phosphor-icons/react'
import { ordensApi } from '../api/recursos'
import { tempoRelativo } from '../lib/formato'
import { ROTULO_LOCAL, localRotulo } from '../lib/rotulos'
import type { LocalOficina, OrdemServico } from '../types/dominio'
import { Botao, Cartao, ErroBox, Input, Selecao, cx } from './ui'

/**
 * Onde o objeto esta e para onde vai.
 *
 * Lugar e status sao eixos independentes: um carro APROVADA pode estar no patio
 * esperando vaga no elevador. Por isso este controle vive separado do fluxo de
 * status, e nao dentro dele.
 *
 * OUTRO abre um campo livre — cada oficina tem um canto com nome proprio, e o
 * backend recusa OUTRO sem dizer onde.
 */
export function ControleLocal({ os }: { os: OrdemServico }) {
  const queryClient = useQueryClient()
  const [destino, setDestino] = useState<LocalOficina>(os.local ?? 'PATIO')
  const [detalhe, setDetalhe] = useState(os.localDetalhe ?? '')
  const [erro, setErro] = useState<string>()

  const mover = useMutation({
    mutationFn: () => ordensApi.moverLocal(os.id, destino, detalhe || undefined),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['ordem', os.id] })
      void queryClient.invalidateQueries({ queryKey: ['ordens'] })
      setErro(undefined)
    },
    onError: (e: Error) => setErro(e.message),
  })

  const mudou = destino !== os.local || (destino === 'OUTRO' && detalhe !== (os.localDetalhe ?? ''))

  return (
    <Cartao className="space-y-3">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-xs font-bold text-tinta-500 uppercase">Onde está</p>
          <p className="mt-1 flex items-center gap-1.5 font-display text-lg font-bold tracking-tight">
            <MapPin size={18} weight="fill" className="shrink-0 text-tinta-600" />
            {localRotulo(os.local, os.localDetalhe)}
          </p>
          {os.localDesde && (
            <p className="text-xs text-tinta-500">aqui {tempoRelativo(os.localDesde)}</p>
          )}
        </div>
      </div>

      {erro && <ErroBox mensagem={erro} />}

      <div className="flex flex-wrap gap-2">
        <Selecao
          value={destino}
          onChange={(e) => setDestino(e.target.value as LocalOficina)}
          className="min-w-36 flex-1"
          aria-label="Mover para"
        >
          {(Object.keys(ROTULO_LOCAL) as LocalOficina[]).map((l) => (
            <option key={l} value={l}>
              {ROTULO_LOCAL[l]}
            </option>
          ))}
        </Selecao>

        {destino === 'OUTRO' && (
          <Input
            value={detalhe}
            onChange={(e) => setDetalhe(e.target.value)}
            placeholder="Onde? Ex.: bancada da funilaria"
            className="min-w-40 flex-1"
            aria-label="Onde exatamente"
          />
        )}

        <Botao
          onClick={() => mover.mutate()}
          disabled={!mudou || mover.isPending}
          className={cx(!mudou && 'opacity-50')}
        >
          {mover.isPending ? 'Movendo…' : 'Mover'}
        </Botao>
      </div>

      {!!os.historicoLocal?.length && (
        <details className="text-sm">
          <summary className="cursor-pointer text-tinta-500 hover:text-tinta-900">
            Histórico de movimentação ({os.historicoLocal.length})
          </summary>
          <ul className="mt-2 space-y-1.5 border-t border-white/8 pt-2">
            {[...os.historicoLocal].reverse().map((m, i) => (
              <li key={`${m.em}-${i}`} className="flex items-baseline justify-between gap-3">
                <span className="min-w-0 text-tinta-600">
                  {localRotulo(m.de, m.deDetalhe)} → {localRotulo(m.para, m.paraDetalhe)}
                  {m.autorNome && <span className="text-tinta-500"> · {m.autorNome}</span>}
                </span>
                <span className="shrink-0 text-xs text-tinta-500">{tempoRelativo(m.em)}</span>
              </li>
            ))}
          </ul>
        </details>
      )}
    </Cartao>
  )
}

/** Etiqueta de prazo: silenciosa no prazo, marcada quando estoura. */
export function EtiquetaPrazo({ os }: { os: OrdemServico }) {
  if (!os.previsaoEntrega) return null

  const atrasada =
    os.status !== 'ENTREGUE' &&
    os.status !== 'CANCELADA' &&
    new Date(os.previsaoEntrega).getTime() < Date.now()

  return (
    <span
      className={cx(
        'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-bold',
        atrasada ? 'bg-white text-black' : 'border border-white/15 text-tinta-600',
      )}
      title={atrasada ? 'Passou da previsão de entrega' : 'Dentro do prazo'}
    >
      {atrasada ? 'Atrasada' : 'Entrega'} {tempoRelativo(os.previsaoEntrega)}
    </span>
  )
}
