import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ordensApi } from '../api/recursos'
import { MapPin, Plus } from '@phosphor-icons/react'
import { EtiquetaPrazo } from '../components/ControleLocal'
import {
  Botao,
  CabecalhoPagina,
  Cartao,
  Carregando,
  EtiquetaPagamento,
  EtiquetaStatus,
  ROTULO_STATUS,
  Vazio,
  cx,
} from '../components/ui'
import { moeda, placaFormatada, tempoRelativo } from '../lib/formato'
import { localRotulo } from '../lib/rotulos'
import type { StatusOS } from '../types/dominio'

/** Saiu da oficina: nao faz sentido dizer onde esta. */
const FINALIZADAS: StatusOS[] = ['ENTREGUE', 'CANCELADA']

const FILTROS: (StatusOS | 'TODAS')[] = [
  'TODAS',
  'ORCAMENTO',
  'APROVADA',
  'EM_EXECUCAO',
  'AGUARDANDO_PECA',
  'PRONTA',
  'ENTREGUE',
]

export function PaginaOrdens() {
  const [filtro, setFiltro] = useState<StatusOS | 'TODAS'>('TODAS')

  const { data: ordens, isLoading } = useQuery({
    queryKey: ['ordens', filtro],
    queryFn: () => ordensApi.listar(filtro === 'TODAS' ? undefined : filtro),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Ordens de Serviço"
        acoes={
          /* A barra do topo ja tem "Nova OS" a partir de sm — aqui so no celular. */
          <Link to="/app/ordens/nova" className="sm:hidden">
            <Botao>
              <Plus size={16} weight="bold" />
              Nova OS
            </Botao>
          </Link>
        }
      />

      {/* Filtros roláveis: cabem na largura do celular sem quebrar. */}
      <div className="-mx-3 flex gap-2 overflow-x-auto px-3 pb-1 sem-barra">
        {FILTROS.map((f) => (
          <button
            key={f}
            onClick={() => setFiltro(f)}
            className={cx(
              'min-h-9 shrink-0 rounded-full border px-3.5 text-sm font-semibold transition-all',
              filtro === f
                ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                : 'border-white/10 bg-white/[0.04] text-tinta-600 hover:border-white/25 hover:text-tinta-900',
            )}
          >
            {f === 'TODAS' ? 'Todas' : ROTULO_STATUS[f]}
          </button>
        ))}
      </div>

      {isLoading ? (
        <Carregando />
      ) : !ordens?.length ? (
        <Cartao>
          <Vazio titulo="Nenhuma OS encontrada" descricao="Ajuste o filtro ou abra uma nova OS." />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {ordens.map((os) => (
            <li key={os.id}>
              <Link to={`/app/ordens/${os.id}`}>
                <Cartao className="tk-card-hover">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        {os.tipoObjeto === 'PECA' ? (
                          /* Peca avulsa nao tem placa: a descricao ocupa a linha inteira. */
                          <span className="truncate text-sm font-bold">{os.objetoDescricao}</span>
                        ) : (
                          <>
                            {/* shrink-0: a placa nunca cede espaco, quem trunca e a descricao. */}
                            <span className="shrink-0 font-mono text-sm font-bold">
                              {placaFormatada(os.veiculoPlaca)}
                            </span>
                            <span className="truncate text-sm text-tinta-500">
                              {os.veiculoDescricao}
                            </span>
                          </>
                        )}
                      </div>
                      <p className="mt-0.5 truncate text-sm text-tinta-600">{os.clienteNome}</p>
                      <p className="mt-1 flex flex-wrap items-center gap-x-1.5 gap-y-1 text-xs text-tinta-400">
                        <span>
                          {os.numero} · {tempoRelativo(os.dataAbertura)}
                        </span>
                        {os.local && !FINALIZADAS.includes(os.status) && (
                          <span className="inline-flex items-center gap-1 text-tinta-500">
                            <MapPin size={11} weight="fill" />
                            {localRotulo(os.local, os.localDetalhe)}
                          </span>
                        )}
                      </p>
                      <div className="mt-1.5">
                        <EtiquetaPrazo os={os} />
                      </div>
                    </div>
                    <div className="flex shrink-0 flex-col items-end gap-1">
                      <EtiquetaStatus status={os.status} />
                      <span className="font-bold">{moeda(os.total)}</span>
                      {os.pagamento && <EtiquetaPagamento status={os.pagamento.status} />}
                    </div>
                  </div>
                </Cartao>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
