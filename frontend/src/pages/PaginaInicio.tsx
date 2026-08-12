import { lazy, Suspense, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, CarProfile, Plus, Warning } from '@phosphor-icons/react'
import type { EChartsOption } from 'echarts'
import { dashboardApi, ordensApi } from '../api/recursos'
import { useAuth } from '../auth/AuthContext'
import type { OrdemServico, StatusOS } from '../types/dominio'
import {
  Botao,
  CabecalhoPagina,
  Cartao,
  Carregando,
  EtiquetaStatus,
  ROTULO_STATUS,
  Vazio,
  cx,
} from '../components/ui'
import { EIXO_ROTULO, FONTE, FONTE_MONO, TINTA, TOOLTIP_BASE } from '../components/temaGrafico'
import { moeda, placaFormatada } from '../lib/formato'

/*
 * O ECharts entra sob demanda: quem abre a landing ou o login nao paga por uma
 * biblioteca de graficos que so o painel usa.
 */
const Grafico = lazy(() => import('../components/Grafico'))

/** Reserva a altura do grafico enquanto ele chega — evita o pulo de layout. */
function EsqueletoGrafico({ altura }: { altura: number }) {
  return <div style={{ height: altura }} className="animate-pulse rounded-xl bg-white/[0.03]" />
}

/** Intensidade do ponto "ao vivo" da baia — sem cor, a luz conta o status. */
const LUZ_BAIA: Record<StatusOS, string> = {
  ORCAMENTO: 'bg-white/30',
  APROVADA: 'bg-white/60',
  EM_EXECUCAO: 'bg-white',
  AGUARDANDO_PECA: 'bg-white/40',
  PRONTA: 'bg-white/80',
  ENTREGUE: 'bg-white/20',
  CANCELADA: 'bg-white/10',
}

/** Ordem do fluxo — as barras seguem o caminho da OS, nao o tamanho. */
const ORDEM_STATUS: StatusOS[] = [
  'ORCAMENTO',
  'APROVADA',
  'EM_EXECUCAO',
  'AGUARDANDO_PECA',
  'PRONTA',
  'ENTREGUE',
  'CANCELADA',
]

const DIA_MES = new Intl.DateTimeFormat('pt-BR', {
  day: '2-digit',
  month: '2-digit',
  timeZone: 'America/Sao_Paulo',
})

function duracaoDesde(iso: string, agora: number): string {
  const ms = Math.max(0, agora - new Date(iso).getTime())
  const s = Math.floor(ms / 1000)
  const hh = String(Math.floor(s / 3600)).padStart(2, '0')
  const mm = String(Math.floor((s % 3600) / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
}

/** R$ 1,2 mil / R$ 340 — rotulo curto para caber no eixo sem virar sopa. */
function moedaCurta(valor: number): string {
  if (valor >= 1000) return `${(valor / 1000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mil`
  return valor.toLocaleString('pt-BR', { maximumFractionDigits: 0 })
}

export function PaginaInicio() {
  const { sessao } = useAuth()

  const { data: resumo, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardApi.resumo,
  })
  const { data: ordens } = useQuery({ queryKey: ['ordens'], queryFn: () => ordensApi.listar() })

  const abertas = useMemo(
    () => ordens?.filter((o) => o.status !== 'ENTREGUE' && o.status !== 'CANCELADA') ?? [],
    [ordens],
  )

  // A OS em destaque no painel-heroi: a que esta sendo trabalhada agora.
  const emDestaque =
    abertas.find((o) => o.status === 'EM_EXECUCAO') ??
    abertas.find((o) => o.status === 'PRONTA') ??
    abertas[0]

  if (isLoading) return <Carregando />

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        sobretitulo={`Olá, ${sessao?.usuario.nome?.split(' ')[0] ?? ''}`}
        titulo={sessao?.oficina?.nomeFantasia ?? 'Track Wheel'}
        acoes={
          /* No desktop a barra do topo ja tem "Nova OS"; aqui so no celular. */
          <Link to="/app/ordens/nova" className="sm:hidden">
            <Botao>
              <Plus size={16} weight="bold" />
              Nova OS
            </Botao>
          </Link>
        }
      />

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <Indicador rotulo="Faturamento hoje" valor={moeda(resumo?.faturamentoDia)} destaque />
        <Indicador rotulo="Faturamento do mês" valor={moeda(resumo?.faturamentoMes)} />
        <Indicador rotulo="Carros no pátio" valor={String(abertas.length)} />
        <Indicador rotulo="Ticket médio" valor={moeda(resumo?.ticketMedio)} />
      </div>

      {!!resumo?.alertasEstoque?.length && (
        <Cartao className="border-l-2 border-l-red-500/80">
          <div className="mb-2 flex items-center justify-between gap-2">
            <p className="flex items-center gap-2 text-sm font-bold text-red-400">
              <Warning size={16} weight="fill" />
              Estoque baixo ({resumo.produtosEstoqueBaixo})
            </p>
            <Link
              to="/app/estoque"
              className="shrink-0 text-xs font-semibold text-tinta-600 underline underline-offset-2 hover:text-tinta-900"
            >
              ver estoque
            </Link>
          </div>
          <ul className="space-y-1">
            {resumo.alertasEstoque.slice(0, 4).map((a) => (
              <li key={a.produtoId} className="flex justify-between text-sm text-tinta-700">
                <span className="truncate pr-2">{a.nome}</span>
                <span className="shrink-0 font-semibold tabular-nums">
                  {a.estoqueAtual} / mín {a.estoqueMinimo}
                </span>
              </li>
            ))}
          </ul>
        </Cartao>
      )}

      <div className="grid gap-4 lg:grid-cols-3">
        <Faturamento ordens={ordens} className="lg:col-span-2" />
        <OsPorStatus ordens={ordens} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {/* Pátio: a grade de baias do modelo de estacionamento. Carro na baia = OS aberta. */}
        <Patio abertas={abertas} className="lg:col-span-2" />

        {/* Painel-heroi: o carro em atendimento agora, com cronometro vivo. */}
        <EmAtendimento os={emDestaque} />
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------ *
 * Graficos
 * ------------------------------------------------------------------ */

/**
 * Faturamento dos ultimos 14 dias. Conta o que foi ENTREGUE, na mesma regra do
 * backend (DashboardService): orcamento aprovado ainda nao e dinheiro em caixa.
 */
function Faturamento({ ordens, className }: { ordens?: OrdemServico[]; className?: string }) {
  const dias = useMemo(() => {
    const hoje = new Date()
    hoje.setHours(0, 0, 0, 0)

    const balde = new Map<string, number>()
    const rotulos: { chave: string; rotulo: string }[] = []
    for (let i = 13; i >= 0; i -= 1) {
      const dia = new Date(hoje)
      dia.setDate(hoje.getDate() - i)
      const chave = dia.toISOString().slice(0, 10)
      balde.set(chave, 0)
      rotulos.push({ chave, rotulo: DIA_MES.format(dia) })
    }

    for (const os of ordens ?? []) {
      if (os.status !== 'ENTREGUE') continue
      const quando = os.dataEntrega ?? os.dataConclusao ?? os.dataAbertura
      const chave = quando.slice(0, 10)
      if (balde.has(chave)) balde.set(chave, (balde.get(chave) ?? 0) + (os.total ?? 0))
    }

    return rotulos.map((r) => ({ ...r, valor: balde.get(r.chave) ?? 0 }))
  }, [ordens])

  const total = dias.reduce((soma, d) => soma + d.valor, 0)
  const ultimoComValor = dias.reduce((idx, d, i) => (d.valor > 0 ? i : idx), -1)

  const opcao = useMemo<EChartsOption>(
    () => ({
      animationDuration: 500,
      tooltip: {
        ...TOOLTIP_BASE,
        trigger: 'axis',
        axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.28)', width: 1 } },
        valueFormatter: (v) => moeda(Number(v)),
      },
      /* Folga a direita e em cima: o rotulo do ultimo ponto precisa caber. */
      grid: { left: 2, right: 58, top: 30, bottom: 0, containLabel: true },
      xAxis: {
        type: 'category',
        data: dias.map((d) => d.rotulo),
        boundaryGap: false,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { ...EIXO_ROTULO, interval: 2 },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: TINTA.grade, width: 1 } },
        axisLabel: { ...EIXO_ROTULO, formatter: (v: number) => moedaCurta(v) },
      },
      series: [
        {
          type: 'line',
          name: 'Faturamento',
          data: dias.map((d) => d.valor),
          smooth: 0.3,
          symbol: 'circle',
          /*
           * O rotulo de um ponto so aparece se o ponto existir: com
           * showSymbol:false o ECharts esconde os dois. Entao o simbolo fica
           * ligado e some por tamanho zero em todo dia que nao e o ultimo.
           */
          showSymbol: true,
          symbolSize: (_valor, params) => (params.dataIndex === ultimoComValor ? 9 : 0),
          lineStyle: { color: TINTA.marca, width: 2 },
          itemStyle: { color: TINTA.marca, borderColor: '#17171a', borderWidth: 2 },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(255,255,255,0.22)' },
                { offset: 1, color: 'rgba(255,255,255,0)' },
              ],
            },
          },
          /* Rotulo direto so no ultimo dia com valor: o resto fica no tooltip. */
          label: {
            show: true,
            position: 'top',
            distance: 8,
            color: TINTA.texto,
            fontFamily: FONTE_MONO,
            fontSize: 11,
            formatter: (p) => (p.dataIndex === ultimoComValor ? moeda(Number(p.value)) : ''),
          },
        },
      ],
    }),
    [dias, ultimoComValor],
  )

  return (
    <Cartao className={cx('p-4 sm:p-5', className)}>
      <div className="mb-1 flex items-start justify-between gap-3">
        <div>
          <h2 className="font-display text-lg font-bold tracking-tight text-tinta-900">
            Faturamento
          </h2>
          <p className="text-xs text-tinta-500">Últimos 14 dias, contando o que foi entregue</p>
        </div>
        <div className="shrink-0 text-right">
          <p className="text-[11px] tracking-[0.14em] text-tinta-500 uppercase">No período</p>
          <p className="font-display text-lg font-bold text-tinta-900">{moeda(total)}</p>
        </div>
      </div>

      {total === 0 ? (
        <Vazio
          titulo="Nada entregue nos últimos 14 dias"
          descricao="O faturamento entra aqui quando uma OS passa para Entregue."
        />
      ) : (
        <Suspense fallback={<EsqueletoGrafico altura={230} />}>
          <Grafico
            opcao={opcao}
            altura={230}
            rotulo={`Faturamento diário dos últimos 14 dias, total de ${moeda(total)}`}
          />
        </Suspense>
      )}
    </Cartao>
  )
}

/**
 * Quantas OS em cada etapa. Barra horizontal em vez de rosca: o nome do status
 * fica legivel no eixo e o numero vai na ponta — ninguem precisa da cor para ler.
 */
function OsPorStatus({ ordens }: { ordens?: OrdemServico[] }) {
  const linhas = useMemo(() => {
    const contagem = new Map<StatusOS, number>()
    for (const os of ordens ?? []) {
      contagem.set(os.status, (contagem.get(os.status) ?? 0) + 1)
    }
    return ORDEM_STATUS.filter((s) => (contagem.get(s) ?? 0) > 0).map((s) => ({
      status: s,
      rotulo: ROTULO_STATUS[s],
      valor: contagem.get(s) ?? 0,
    }))
  }, [ordens])

  const maximo = Math.max(1, ...linhas.map((l) => l.valor))

  const opcao = useMemo<EChartsOption>(
    () => ({
      animationDuration: 500,
      tooltip: {
        ...TOOLTIP_BASE,
        trigger: 'item',
        formatter: '{b}<br/><strong>{c}</strong> na etapa',
      },
      grid: { left: 0, right: 34, top: 4, bottom: 0, containLabel: true },
      xAxis: { type: 'value', show: true, splitLine: { show: false }, axisLabel: { show: false }, max: maximo },
      yAxis: {
        type: 'category',
        data: linhas.map((l) => l.rotulo),
        inverse: true,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#d0d0d5', fontSize: 12, fontFamily: FONTE },
      },
      series: [
        {
          type: 'bar',
          name: 'Ordens',
          data: linhas.map((l) => l.valor),
          barWidth: 12,
          barCategoryGap: '58%',
          itemStyle: { color: TINTA.barra, borderRadius: [0, 4, 4, 0] },
          showBackground: true,
          backgroundStyle: { color: 'rgba(255,255,255,0.045)', borderRadius: [0, 4, 4, 0] },
          label: {
            show: true,
            position: 'right',
            distance: 8,
            color: TINTA.texto,
            fontFamily: FONTE_MONO,
            fontSize: 12,
            formatter: '{c}',
          },
        },
      ],
    }),
    [linhas, maximo],
  )

  return (
    <Cartao className="self-start p-4 sm:p-5">
      <div className="mb-3">
        <h2 className="font-display text-lg font-bold tracking-tight text-tinta-900">
          OS por etapa
        </h2>
        <p className="text-xs text-tinta-500">
          {ordens?.length ?? 0} {ordens?.length === 1 ? 'ordem no total' : 'ordens no total'}
        </p>
      </div>

      {linhas.length === 0 ? (
        <Vazio titulo="Nenhuma OS ainda" descricao="Abra a primeira e ela aparece aqui." />
      ) : (
        <Suspense fallback={<EsqueletoGrafico altura={Math.max(150, linhas.length * 42)} />}>
          <Grafico
            opcao={opcao}
            altura={Math.max(150, linhas.length * 42)}
            rotulo={`Ordens por etapa: ${linhas.map((l) => `${l.rotulo}, ${l.valor}`).join('; ')}`}
          />
        </Suspense>
      )}
    </Cartao>
  )
}

/* ------------------------------------------------------------------ *
 * Pátio
 * ------------------------------------------------------------------ */

function Patio({ abertas, className }: { abertas: OrdemServico[]; className?: string }) {
  // Sempre desenha uma grade cheia: baias ocupadas primeiro, depois vagas livres.
  const totalBaias = Math.max(12, Math.ceil((abertas.length + 2) / 4) * 4)
  const baias = Array.from({ length: totalBaias }, (_, i) => abertas[i])

  return (
    <Cartao className={cx('p-4 sm:p-5', className)}>
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="font-display text-lg font-bold tracking-tight text-tinta-900">
            Pátio da oficina
          </h2>
          <p className="text-xs text-tinta-500">
            {abertas.length}{' '}
            {abertas.length === 1 ? 'carro em atendimento' : 'carros em atendimento'}
          </p>
        </div>
        <Link
          to="/app/ordens"
          className="flex items-center gap-1 text-sm font-semibold text-tinta-600 transition-colors hover:text-tinta-900"
        >
          Ver OS <ArrowRight size={15} weight="bold" />
        </Link>
      </div>

      {abertas.length === 0 ? (
        <Vazio
          titulo="Pátio vazio"
          descricao="Quando um carro entrar na oficina, abra uma OS e ele aparece aqui."
          acao={
            <Link to="/app/ordens/nova">
              <Botao>Abrir primeira OS</Botao>
            </Link>
          }
        />
      ) : (
        <div className="grid grid-cols-3 gap-2.5 sm:grid-cols-4">
          {baias.map((os, i) => (
            <Baia key={os?.id ?? `livre-${i}`} os={os} indice={i} />
          ))}
        </div>
      )}
    </Cartao>
  )
}

/** Uma baia do pátio: ocupada (carro + placa + ponto de luz) ou livre. */
function Baia({ os, indice }: { os?: OrdemServico; indice: number }) {
  const rotulo = `${String.fromCharCode(65 + Math.floor(indice / 4))}${String((indice % 4) + 1).padStart(2, '0')}`

  if (!os) {
    return (
      <div className="flex aspect-[4/3.4] flex-col justify-between rounded-2xl border border-dashed border-white/10 p-2.5">
        <span className="font-mono text-[10px] font-bold tracking-wide text-tinta-400">
          {rotulo}
        </span>
        <span className="text-center text-[11px] font-medium text-tinta-400">Livre</span>
        <span />
      </div>
    )
  }

  const emExecucao = os.status === 'EM_EXECUCAO'
  return (
    <Link
      to={`/app/ordens/${os.id}`}
      className={cx(
        'group flex aspect-[4/3.4] flex-col justify-between rounded-2xl border p-2.5 transition-all hover:-translate-y-0.5',
        emExecucao
          ? 'border-white/40 bg-white/[0.08] shadow-[var(--tk-glow)]'
          : 'border-white/10 bg-white/[0.03] hover:border-white/25 hover:bg-white/[0.06]',
      )}
    >
      <div className="flex items-center justify-between">
        <span className="font-mono text-[10px] font-bold tracking-wide text-tinta-500">
          {rotulo}
        </span>
        <span className={cx('size-2 rounded-full', LUZ_BAIA[os.status], emExecucao && 'tk-pulso')} />
      </div>
      <CarProfile
        size={30}
        weight={emExecucao ? 'fill' : 'regular'}
        className={cx(
          'mx-auto',
          emExecucao ? 'text-tinta-900' : 'text-tinta-500 group-hover:text-tinta-700',
        )}
      />
      <span className="truncate text-center font-mono text-[11px] font-bold text-tinta-900">
        {placaFormatada(os.veiculoPlaca)}
      </span>
    </Link>
  )
}

function EmAtendimento({ os }: { os?: OrdemServico }) {
  const [agora, setAgora] = useState(() => Date.now())
  useEffect(() => {
    const t = setInterval(() => setAgora(Date.now()), 1000)
    return () => clearInterval(t)
  }, [])

  if (!os) {
    return (
      <div className="tk-hero flex flex-col items-center justify-center gap-2 self-start p-6 text-center">
        <CarProfile size={32} className="text-tinta-400" />
        <p className="text-sm font-semibold text-tinta-700">Nenhum carro em atendimento</p>
        <p className="text-xs text-tinta-500">Abra uma OS para começar a cronometrar.</p>
      </div>
    )
  }

  // So pulsa o que esta mesmo na rampa; um orcamento parado no patio nao "atende".
  const naRampa = os.status === 'EM_EXECUCAO'

  return (
    <div className="tk-hero flex flex-col gap-4 self-start p-5">
      <div className="flex items-center justify-between">
        <span className="inline-flex items-center gap-1.5 rounded-full bg-white/[0.07] px-2.5 py-1 text-[11px] font-bold tracking-[0.14em] text-tinta-600 uppercase">
          <span
            className={cx('size-1.5 rounded-full bg-white', naRampa ? 'tk-pulso' : 'opacity-40')}
          />
          {naRampa ? 'Em atendimento' : 'No pátio'}
        </span>
        <EtiquetaStatus status={os.status} />
      </div>

      <div>
        <p className="font-mono text-2xl font-bold tracking-tight text-tinta-900">
          {placaFormatada(os.veiculoPlaca)}
        </p>
        <p className="truncate text-sm text-tinta-600">{os.veiculoDescricao}</p>
        <p className="truncate text-sm text-tinta-500">{os.clienteNome}</p>
      </div>

      {/* Cronometro vivo: tempo desde a abertura, como o "Current parked" do modelo. */}
      <div className="rounded-2xl bg-black/40 p-4 text-center ring-1 ring-white/10">
        <p className="text-[11px] tracking-[0.18em] text-tinta-500 uppercase">No pátio há</p>
        <p className="mt-1 font-mono text-3xl font-bold tabular-nums text-tinta-900">
          {duracaoDesde(os.dataAbertura, agora)}
        </p>
      </div>

      <div className="flex items-center justify-between">
        <div>
          <p className="text-[11px] tracking-[0.18em] text-tinta-500 uppercase">Total</p>
          <p className="text-lg font-bold text-tinta-900">{moeda(os.total)}</p>
        </div>
        <Link to={`/app/ordens/${os.id}`}>
          <Botao>
            Ver OS <ArrowRight size={16} weight="bold" />
          </Botao>
        </Link>
      </div>
    </div>
  )
}

/** O indicador em destaque inverte para branco: o unico acento do monocromatico. */
function Indicador({
  rotulo,
  valor,
  destaque,
}: {
  rotulo: string
  valor: string
  destaque?: boolean
}) {
  return (
    <Cartao className={cx(destaque && 'border-white/70 !bg-white shadow-[var(--tk-glow)]')}>
      <p className={cx('text-xs font-medium', destaque ? 'text-black/55' : 'text-tinta-500')}>
        {rotulo}
      </p>
      <p
        className={cx(
          'mt-1 truncate font-display text-lg font-bold tracking-tight',
          destaque ? 'text-black' : 'text-tinta-900',
        )}
      >
        {valor}
      </p>
    </Cartao>
  )
}
