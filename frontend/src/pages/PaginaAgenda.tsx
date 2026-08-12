import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { agendaApi } from '../api/recursos'
import { CabecalhoPagina, Cartao, Carregando, Input, Vazio } from '../components/ui'
import { placaFormatada } from '../lib/formato'
import { ROTULO_AGENDAMENTO } from '../lib/rotulos'

const HORA = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short', timeZone: 'America/Sao_Paulo' })

export function PaginaAgenda() {
  const [dia, setDia] = useState(() => new Date().toISOString().slice(0, 10))

  const { data: agendamentos, isLoading } = useQuery({
    queryKey: ['agenda', dia],
    queryFn: () => agendaApi.listar({ dia }),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina titulo="Agenda" descricao="Os agendamentos do dia, em ordem de horário." />

      <Input type="date" value={dia} onChange={(e) => setDia(e.target.value)} />

      {isLoading ? (
        <Carregando />
      ) : !agendamentos?.length ? (
        <Cartao>
          <Vazio
            titulo="Nada agendado para este dia"
            descricao="Os agendamentos do dia aparecem aqui, em ordem de horário."
          />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {agendamentos.map((a) => (
            <li key={a.id}>
              <Cartao>
                <div className="flex gap-3">
                  <div className="shrink-0 text-center">
                    <p className="font-mono text-base font-bold text-tinta-800">
                      {HORA.format(new Date(a.inicio))}
                    </p>
                    <p className="text-xs text-tinta-500">{a.duracaoEstimadaMinutos} min</p>
                  </div>
                  <div className="min-w-0 flex-1 border-l border-white/8 pl-3">
                    <p className="truncate font-semibold">{a.servicoPrevisto ?? 'Serviço'}</p>
                    <p className="truncate text-sm text-tinta-500">
                      {a.clienteNome}
                      {a.veiculoPlaca && ` · ${placaFormatada(a.veiculoPlaca)}`}
                    </p>
                  </div>
                  <span className="shrink-0 self-start rounded-full border border-white/10 bg-white/[0.05] px-2.5 py-1 text-[11px] font-semibold text-tinta-600">
                    {ROTULO_AGENDAMENTO[a.status] ?? a.status}
                  </span>
                </div>
              </Cartao>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
