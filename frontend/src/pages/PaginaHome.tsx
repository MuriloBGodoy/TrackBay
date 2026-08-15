import { Link } from 'react-router-dom'
import {
  ArrowRight,
  CarProfile,
  CheckCircle,
  ClipboardText,
  CurrencyCircleDollar,
  FilePdf,
  Package,
  Sparkle,
  Timer,
} from '@phosphor-icons/react'
import { MarcaArte } from '../components/Marca'
import { BarraPublica, ChamadaFinal, Rodape, Secao, useEntrada } from '../components/site'
import { Botao } from '../components/ui'

/**
 * ===== Home =====
 *
 * Porta de entrada, nao carta de vendas. O produto e por assinatura: quem chega
 * aqui em geral ja comprou e quer entrar, entao a home mostra a marca, diz em
 * seis cartoes o que a plataforma faz e poe o botao de entrar em tres lugares.
 *
 * O argumento longo — a simulacao de campos por ramo, o fluxo da OS, o catalogo
 * de ramos, os numeros — mora em `PaginaComoFunciona`, a um clique daqui. Antes
 * era tudo uma pagina so, com oito blocos e cinco telas de rolagem.
 */

const RECURSOS = [
  {
    Icone: CarProfile,
    titulo: 'Pátio da oficina',
    texto:
      'Cada carro em atendimento ganha uma baia na grade. Quem está no balcão vê a oficina inteira numa olhada — e o cronômetro de quem está na rampa agora.',
  },
  {
    Icone: ClipboardText,
    titulo: 'Campos que mudam com o ramo',
    texto:
      'O formulário da OS vem do banco, versionado. Editar o catálogo cria uma versão nova, e as OS antigas continuam abrindo exatamente com o formulário de quando foram preenchidas.',
  },
  {
    Icone: FilePdf,
    titulo: 'PDF e WhatsApp',
    texto:
      'Orçamento e OS viram PDF com os dados da oficina, veículo, itens, totais e garantia. Um toque manda o resumo formatado no WhatsApp do cliente.',
  },
  {
    Icone: Package,
    titulo: 'Estoque com baixa automática',
    texto:
      'Peça própria lançada na OS sai do estoque na entrega, com movimentação auditável e alerta quando o saldo encosta no mínimo.',
  },
  {
    Icone: CurrencyCircleDollar,
    titulo: 'Pagamento dividido',
    texto:
      'R$ 200 no PIX mais R$ 300 em 2x no crédito, com taxa de maquininha. O saldo da OS acompanha cada parcela recebida.',
  },
  {
    Icone: Timer,
    titulo: 'Histórico por placa',
    texto:
      'Digite a placa e veja tudo o que aquele carro já fez na sua oficina — com valores, datas e quem executou.',
  },
]

/**
 * O palco da marca: a arte dentro dos aneis de luz, com um cartao de OS
 * flutuando — a promessa e o produto juntos. Quem se move aqui e a luz.
 */
function PalcoMarca() {
  return (
    <div className="relative mx-auto flex w-full max-w-md items-center justify-center">
      {/* Aneis concentricos: o facho de luz do estudio */}
      <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
        <div className="size-[300px] rounded-full border border-white/8 sm:size-[380px]" />
      </div>
      <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
        <div className="size-[220px] rounded-full border border-white/12 sm:size-[280px]" />
      </div>
      <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
        <div className="size-[340px] rounded-full bg-[radial-gradient(circle,rgba(255,255,255,0.10),transparent_65%)] blur-xl sm:size-[440px]" />
      </div>

      <div className="relative flex flex-col items-center gap-6 py-10">
        <MarcaArte className="w-72 drop-shadow-[0_24px_60px_rgba(0,0,0,0.75)] sm:w-80" />

        {/* Recorte do produto, para a promessa nao ficar so no logo */}
        <div className="tk-glass w-full max-w-[19rem] rounded-2xl p-3.5 shadow-[var(--tk-shadow-lg)]">
          <div className="flex items-center justify-between gap-2">
            <span className="inline-flex items-center gap-1.5 text-[10px] font-bold tracking-[0.14em] text-tinta-500 uppercase">
              <span className="tk-pulso size-1.5 rounded-full bg-white" />
              Em execução
            </span>
            <span className="font-mono text-[11px] text-tinta-500">OS 2026-0001</span>
          </div>
          <p className="mt-2 font-mono text-2xl font-bold tracking-tight text-tinta-900">BRA2E19</p>
          <p className="truncate text-xs text-tinta-500">
            Mercedes-Benz Accelo 1016 · Suspensão e freios
          </p>
          <div className="mt-3 flex items-end justify-between gap-3 border-t border-white/8 pt-3">
            <span className="text-[10px] tracking-[0.16em] text-tinta-500 uppercase">
              No pátio há
            </span>
            <span className="font-mono text-lg font-bold tabular-nums text-tinta-900">02:14:37</span>
          </div>
        </div>
      </div>
    </div>
  )
}

function Heroi() {
  const { destino, rotuloEntrar } = useEntrada()

  return (
    <section className="relative overflow-hidden">
      {/* Malha tecnica esmaecida nas bordas — a planta baixa do pátio */}
      <div className="tk-malha pointer-events-none absolute inset-0" />

      <div className="relative mx-auto grid max-w-6xl items-center gap-12 px-4 pt-10 pb-16 sm:px-6 sm:pt-16 sm:pb-24 lg:grid-cols-[1.05fr_1fr]">
        <div>
          <span
            className="tk-entra inline-flex items-center gap-1.5 rounded-full border border-white/12 bg-white/[0.05] px-3 py-1.5 text-[11px] font-bold tracking-[0.16em] text-tinta-600 uppercase"
            style={{ animationDelay: '40ms' }}
          >
            <Sparkle size={13} weight="fill" />
            Gestão para oficinas mecânicas
          </span>

          <h1
            className="tk-entra mt-6 font-display text-[38px] leading-[1.05] font-bold tracking-tight text-balance text-tinta-900 sm:text-[58px]"
            style={{ animationDelay: '120ms' }}
          >
            Cada oficina tem sua ficha.
            <span className="block text-tinta-500">A sua já nasce pronta.</span>
          </h1>

          <p
            className="tk-entra mt-6 max-w-xl text-[17px] leading-relaxed text-tinta-600"
            style={{ animationDelay: '200ms' }}
          >
            O Track Bay é o sistema de ordem de serviço que se adapta ao ramo da sua oficina. Quem
            faz revisão vê os campos da revisão; quem faz suspensão vê os dela. Cliente, veículo,
            peça e pagamento continuam sendo os mesmos de sempre.
          </p>

          <div className="tk-entra mt-8 flex flex-wrap gap-3" style={{ animationDelay: '280ms' }}>
            <Link to={destino}>
              <Botao tamanho="grande">
                {rotuloEntrar}
                <ArrowRight size={18} weight="bold" />
              </Botao>
            </Link>
            <Link to="/como-funciona">
              <Botao tamanho="grande" variante="secundario">
                Como funciona
              </Botao>
            </Link>
          </div>

          <ul
            className="tk-entra mt-10 flex flex-wrap gap-x-6 gap-y-2 text-sm text-tinta-500"
            style={{ animationDelay: '360ms' }}
          >
            {['Funciona no celular do mecânico', 'Instala como app', 'Sem papel no balcão'].map(
              (item) => (
                <li key={item} className="flex items-center gap-1.5">
                  <CheckCircle size={15} weight="fill" className="text-tinta-700" />
                  {item}
                </li>
              ),
            )}
          </ul>
        </div>

        <div className="tk-entra" style={{ animationDelay: '200ms' }}>
          <PalcoMarca />
        </div>
      </div>
    </section>
  )
}

function Recursos() {
  return (
    <Secao
      id="recursos"
      sobretitulo="O que a plataforma faz"
      titulo="O que a oficina faz no dia, registrado"
      descricao="Nada de módulo que ninguém abre. Cada tela existe porque alguém no balcão precisava dela."
      acao={
        <Link
          to="/como-funciona"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-tinta-700 underline-offset-4 transition-colors hover:text-tinta-900 hover:underline"
        >
          Ver como funciona por dentro
          <ArrowRight size={15} weight="bold" />
        </Link>
      }
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {RECURSOS.map(({ Icone, titulo, texto }) => (
          <article key={titulo} className="tk-card tk-card-hover flex flex-col gap-3 p-5 sm:p-6">
            <span className="flex size-11 items-center justify-center rounded-xl bg-white/[0.06] text-tinta-900 ring-1 ring-white/10">
              <Icone size={21} weight="duotone" />
            </span>
            <h3 className="font-display text-lg font-bold tracking-tight text-tinta-900">
              {titulo}
            </h3>
            <p className="text-sm leading-relaxed text-tinta-500">{texto}</p>
          </article>
        ))}
      </div>
    </Secao>
  )
}

export function PaginaHome() {
  return (
    <div className="flex min-h-full flex-col">
      <BarraPublica />
      <main className="flex-1">
        <Heroi />
        <Recursos />
        <ChamadaFinal />
      </main>
      <Rodape />
    </div>
  )
}
