import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { veiculosApi } from '../api/recursos'
import { CabecalhoPagina, Cartao, Carregando, EtiquetaStatus, Input, Vazio } from '../components/ui'
import { moeda, placaFormatada, tempoRelativo } from '../lib/formato'
import { ROTULO_TIPO_VEICULO } from '../lib/rotulos'

export function PaginaVeiculos() {
  const [params] = useSearchParams()
  const [busca, setBusca] = useState(params.get('busca') ?? '')
  const [selecionado, setSelecionado] = useState<string>()

  const { data: veiculos, isLoading } = useQuery({
    queryKey: ['veiculos', busca],
    queryFn: () => veiculosApi.listar({ busca: busca || undefined }),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina titulo="Veículos" descricao="Busque pela placa para abrir o histórico do carro." />

      <Input
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        placeholder="Buscar por placa, marca ou modelo"
        autoFocus
      />

      {isLoading ? (
        <Carregando />
      ) : !veiculos?.length ? (
        <Cartao>
          <Vazio titulo="Nenhum veículo encontrado" descricao="Tente outra placa ou modelo." />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {veiculos.map((v) => (
            <li key={v.id}>
              <Cartao>
                <button
                  onClick={() => setSelecionado(selecionado === v.id ? undefined : v.id)}
                  className="flex w-full items-start justify-between gap-3 text-left"
                >
                  <div className="min-w-0">
                    <p className="font-mono text-lg font-bold">{placaFormatada(v.placa)}</p>
                    <p className="truncate text-sm text-tinta-600">
                      {v.marca} {v.modelo} {v.anoModelo && `· ${v.anoModelo}`}
                    </p>
                    <p className="text-xs text-tinta-400">
                      {v.kmAtual ? `${v.kmAtual.toLocaleString('pt-BR')} km · ` : ''}
                      {ROTULO_TIPO_VEICULO[v.tipoVeiculo]}
                    </p>
                  </div>
                  <span className="text-sm text-tinta-600">
                    {selecionado === v.id ? 'fechar' : 'histórico'}
                  </span>
                </button>

                {selecionado === v.id && <HistoricoVeiculo veiculoId={v.id} />}
              </Cartao>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

/** Historico completo da placa: toda OS que aquele carro ja teve. */
function HistoricoVeiculo({ veiculoId }: { veiculoId: string }) {
  const { data: historico, isLoading } = useQuery({
    queryKey: ['historico', veiculoId],
    queryFn: () => veiculosApi.historico(veiculoId),
  })

  if (isLoading) return <Carregando texto="Buscando histórico…" />

  if (!historico?.length) {
    return (
      <p className="mt-3 border-t border-white/8 pt-3 text-sm text-tinta-500">
        Nenhuma OS registrada para este veículo.
      </p>
    )
  }

  return (
    <ul className="mt-3 space-y-2 border-t border-white/8 pt-3">
      {historico.map((os) => (
        <li key={os.id}>
          <Link to={`/app/ordens/${os.id}`} className="flex items-center justify-between gap-2 text-sm">
            <span className="min-w-0">
              <span className="font-semibold">{os.numero}</span>
              <span className="ml-2 text-tinta-400">{tempoRelativo(os.dataAbertura)}</span>
            </span>
            <span className="flex shrink-0 items-center gap-2">
              <EtiquetaStatus status={os.status} />
              <span className="font-semibold">{moeda(os.total)}</span>
            </span>
          </Link>
        </li>
      ))}
    </ul>
  )
}
