import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { estoqueApi } from '../api/recursos'
import { CabecalhoPagina, Cartao, Carregando, Input, Vazio, cx } from '../components/ui'
import { moeda } from '../lib/formato'
import { ROTULO_MOVIMENTACAO } from '../lib/rotulos'

export function PaginaEstoque() {
  const [busca, setBusca] = useState('')

  const { data: produtos, isLoading } = useQuery({
    queryKey: ['produtos', busca],
    queryFn: () => estoqueApi.produtos(busca || undefined),
  })

  const { data: movimentacoes } = useQuery({
    queryKey: ['movimentacoes'],
    queryFn: () => estoqueApi.movimentacoes(),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina titulo="Estoque" descricao="Peças e produtos disponíveis para venda no balcão." />

      <Input
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        placeholder="Buscar por nome, código ou marca"
      />

      {isLoading ? (
        <Carregando />
      ) : !produtos?.length ? (
        <Cartao>
          <Vazio titulo="Nenhum produto" descricao="Cadastre peças e produtos para vender no balcão." />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {produtos.map((p) => (
            <li key={p.id}>
              {/* Vermelho so onde ha risco de parar o serviço — igual ao alerta do painel. */}
              <Cartao className={cx(p.estoqueBaixo && 'border-l-2 border-l-red-500/80')}>
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate font-semibold">{p.nome}</p>
                    <p className="text-xs text-tinta-400">
                      {p.codigoInterno} {p.marca && `· ${p.marca}`} {p.localizacao && `· ${p.localizacao}`}
                    </p>
                    <p className="mt-1 text-sm">
                      <span className="font-bold text-tinta-700">
                        {moeda(p.precoVenda)}
                      </span>
                      {p.margem ? (
                        <span className="ml-2 text-xs text-tinta-400">margem {p.margem}%</span>
                      ) : null}
                    </p>
                  </div>
                  <div className="shrink-0 text-right">
                    <p className="text-lg font-extrabold tabular-nums text-tinta-900">
                      {p.estoqueAtual}
                    </p>
                    <p className="text-xs text-tinta-400">mín {p.estoqueMinimo}</p>
                    {p.estoqueBaixo && (
                      <span className="mt-1 inline-flex rounded-full bg-red-500/15 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-red-400">
                        repor
                      </span>
                    )}
                  </div>
                </div>
              </Cartao>
            </li>
          ))}
        </ul>
      )}

      {!!movimentacoes?.length && (
        <section>
          <h2 className="mb-2 font-bold">Movimentações recentes</h2>
          <Cartao>
            <ul className="space-y-2">
              {movimentacoes.slice(0, 8).map((m) => (
                <li key={m.id} className="flex items-start justify-between gap-3 text-sm">
                  <div className="min-w-0">
                    <p className="truncate">{m.produtoNome}</p>
                    <p className="truncate text-xs text-tinta-400">
                      {ROTULO_MOVIMENTACAO[m.tipo]}
                      {m.motivo ? ` · ${m.motivo}` : ''}
                    </p>
                  </div>
                  <div className="shrink-0 text-right">
                    {/* O sinal +/- ja diz a direcao; o peso so ajuda a varrer a lista. */}
                    <span
                      className={cx(
                        'font-bold tabular-nums',
                        m.tipo === 'SAIDA' ? 'text-tinta-500' : 'text-tinta-900',
                      )}
                    >
                      {m.tipo === 'SAIDA' ? '−' : '+'}
                      {m.quantidade}
                    </span>
                    <p className="text-xs text-tinta-400">
                      {m.saldoAnterior} → {m.saldoPosterior}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          </Cartao>
        </section>
      )}
    </div>
  )
}
