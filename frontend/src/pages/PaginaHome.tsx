import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  CarProfile,
  CheckCircle,
  ClipboardText,
  CurrencyCircleDollar,
  DeviceMobile,
  FilePdf,
  ListChecks,
  Package,
  ShieldCheck,
  Sparkle,
  Timer,
  WhatsappLogo,
} from '@phosphor-icons/react'
import { useAuth } from '../auth/AuthContext'
import { MarcaSimbolo, MarcaTexto } from '../components/Marca'
import { PromptInstalar } from '../components/PromptInstalar'
import { Botao, cx } from '../components/ui'

/* ------------------------------------------------------------------ *
 * Conteudo
 * ------------------------------------------------------------------ */

const SECOES = [
  { id: 'diferencial', rotulo: 'O diferencial' },
  { id: 'fluxo', rotulo: 'Fluxo da OS' },
  { id: 'recursos', rotulo: 'Recursos' },
  { id: 'ramos', rotulo: 'Ramos' },
]

/**
 * Amostra real do catalogo de campos que o backend semeia por ramo
 * (domain/seed/TemplatesPadrao.java). E o argumento de venda inteiro: a mesma
 * OS muda de cara conforme a oficina.
 */
const RAMOS = [
  {
    valor: 'RADIADOR',
    rotulo: 'Radiador',
    total: 15,
    campos: [
      { rotulo: 'Tipo de radiador', tipo: 'SELECT', grupo: 'Diagnóstico', obrigatorio: true },
      { rotulo: 'Houve vazamento?', tipo: 'BOOLEANO', grupo: 'Diagnóstico' },
      { rotulo: 'Local do vazamento', tipo: 'SELECT', grupo: 'Diagnóstico', condicional: true },
      { rotulo: 'Teste de pressão (bar)', tipo: 'DECIMAL', grupo: 'Testes' },
      { rotulo: 'Serviço executado', tipo: 'MULTI_SELECT', grupo: 'Serviço', obrigatorio: true },
      { rotulo: 'Foto do radiador', tipo: 'FOTO', grupo: 'Evidências' },
    ],
  },
  {
    valor: 'SUSPENSAO_FREIOS',
    rotulo: 'Suspensão e freios',
    total: 9,
    campos: [
      { rotulo: 'Componente', tipo: 'MULTI_SELECT', grupo: 'Diagnóstico', obrigatorio: true },
      { rotulo: 'Espessura da pastilha dianteira (mm)', tipo: 'DECIMAL', grupo: 'Medições' },
      { rotulo: 'Espessura da pastilha traseira (mm)', tipo: 'DECIMAL', grupo: 'Medições' },
      { rotulo: 'Estado do disco', tipo: 'SELECT', grupo: 'Medições' },
      { rotulo: 'Necessita alinhamento?', tipo: 'BOOLEANO', grupo: 'Serviço' },
      { rotulo: 'Observações técnicas', tipo: 'TEXTO_LONGO', grupo: 'Serviço' },
    ],
  },
  {
    valor: 'AR_CONDICIONADO',
    rotulo: 'Ar-condicionado',
    total: 9,
    campos: [
      { rotulo: 'Tipo de gás', tipo: 'SELECT', grupo: 'Serviço', obrigatorio: true },
      { rotulo: 'Carga de gás (g)', tipo: 'NUMERO', grupo: 'Serviço' },
      { rotulo: 'Pressão lado alta (PSI)', tipo: 'DECIMAL', grupo: 'Testes' },
      { rotulo: 'Pressão lado baixa (PSI)', tipo: 'DECIMAL', grupo: 'Testes' },
      { rotulo: 'Temperatura na saída (°C)', tipo: 'DECIMAL', grupo: 'Testes' },
      { rotulo: 'Higienização realizada?', tipo: 'BOOLEANO', grupo: 'Serviço' },
    ],
  },
  {
    valor: 'PNEUS_ALINHAMENTO',
    rotulo: 'Pneus e alinhamento',
    total: 14,
    campos: [
      { rotulo: 'Medida do pneu', tipo: 'TEXTO', grupo: 'Pneus', obrigatorio: true },
      { rotulo: 'Posições trocadas', tipo: 'MULTI_SELECT', grupo: 'Pneus' },
      { rotulo: 'Sulco dianteiro esquerdo (mm)', tipo: 'DECIMAL', grupo: 'Medições' },
      { rotulo: 'Cambagem antes', tipo: 'TEXTO', grupo: 'Alinhamento' },
      { rotulo: 'Convergência depois', tipo: 'TEXTO', grupo: 'Alinhamento' },
      { rotulo: 'Balanceamento realizado?', tipo: 'BOOLEANO', grupo: 'Serviço' },
    ],
  },
  {
    valor: 'TROCA_OLEO',
    rotulo: 'Troca de óleo',
    total: 7,
    campos: [
      { rotulo: 'Tipo de óleo', tipo: 'SELECT', grupo: 'Serviço', obrigatorio: true },
      { rotulo: 'Viscosidade', tipo: 'SELECT', grupo: 'Serviço', obrigatorio: true },
      { rotulo: 'Quantidade (L)', tipo: 'DECIMAL', grupo: 'Serviço', obrigatorio: true },
      { rotulo: 'Filtros trocados', tipo: 'MULTI_SELECT', grupo: 'Serviço' },
      { rotulo: 'Km da próxima troca', tipo: 'NUMERO', grupo: 'Retorno', obrigatorio: true },
      { rotulo: 'Data da próxima troca', tipo: 'DATA', grupo: 'Retorno' },
    ],
  },
  {
    valor: 'INJECAO_ELETRONICA',
    rotulo: 'Injeção eletrônica',
    total: 7,
    campos: [
      { rotulo: 'Códigos de falha', tipo: 'TEXTO_LONGO', grupo: 'Diagnóstico' },
      { rotulo: 'Scanner usado', tipo: 'TEXTO', grupo: 'Diagnóstico' },
      { rotulo: 'Teste de bicos', tipo: 'SELECT', grupo: 'Testes' },
      { rotulo: 'Limpeza realizada', tipo: 'MULTI_SELECT', grupo: 'Serviço' },
      { rotulo: 'Pressão de combustível (bar)', tipo: 'DECIMAL', grupo: 'Testes' },
      { rotulo: 'Apagou as falhas?', tipo: 'BOOLEANO', grupo: 'Serviço' },
    ],
  },
]

const OUTROS_RAMOS = [
  'Mecânica geral',
  'Funilaria e pintura',
  'Elétrica automotiva',
  'Outro (você monta o seu)',
]

const FLUXO = [
  { rotulo: 'Orçamento', nota: 'sai em PDF com linha de assinatura' },
  { rotulo: 'Aprovada', nota: 'grava a data do aceite' },
  { rotulo: 'Em execução', nota: 'cronômetro correndo no pátio' },
  { rotulo: 'Aguardando peça', nota: 'volta para execução quando chegar' },
  { rotulo: 'Pronta', nota: 'cliente avisado pelo WhatsApp' },
  { rotulo: 'Entregue', nota: 'baixa o estoque das peças próprias' },
]

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

const NUMEROS = [
  { valor: '9', rotulo: 'ramos com catálogo pronto' },
  { valor: '12', rotulo: 'tipos de campo, da foto à assinatura' },
  { valor: '0', rotulo: 'campo escrito no código' },
  { valor: '100%', rotulo: 'em português, feito para o Brasil' },
]

/* ------------------------------------------------------------------ *
 * Utilidades da pagina
 * ------------------------------------------------------------------ */

/**
 * Revela o bloco quando ele entra na tela. Sem isto a landing inteira aparece
 * de uma vez e a rolagem fica sem ritmo.
 */
function useRevelar<T extends HTMLElement>() {
  const alvo = useRef<T>(null)
  const [visivel, setVisivel] = useState(false)

  useEffect(() => {
    const no = alvo.current
    if (!no) return
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (entrada.isIntersecting) {
          setVisivel(true)
          observador.disconnect()
        }
      },
      { rootMargin: '-60px' },
    )
    observador.observe(no)
    return () => observador.disconnect()
  }, [])

  return { alvo, visivel }
}

function Secao({
  id,
  sobretitulo,
  titulo,
  descricao,
  acao,
  children,
  className,
}: {
  id?: string
  sobretitulo: string
  titulo: ReactNode
  descricao?: string
  acao?: ReactNode
  children: ReactNode
  className?: string
}) {
  const { alvo, visivel } = useRevelar<HTMLElement>()

  return (
    <section
      id={id}
      ref={alvo}
      className={cx(
        'mx-auto w-full max-w-6xl scroll-mt-24 px-4 py-16 transition-all duration-700 sm:px-6 sm:py-24',
        visivel ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0',
        className,
      )}
    >
      <header className="mb-10 max-w-2xl sm:mb-14">
        <p className="mb-3 text-[11px] font-bold tracking-[0.24em] text-tinta-500 uppercase">
          {sobretitulo}
        </p>
        <h2 className="tk-titulo-luz font-display text-3xl font-bold tracking-tight text-balance sm:text-[42px] sm:leading-[1.08]">
          {titulo}
        </h2>
        {descricao && (
          <p className="mt-4 text-base leading-relaxed text-tinta-600">{descricao}</p>
        )}
        {acao && <div className="mt-6">{acao}</div>}
      </header>
      {children}
    </section>
  )
}

/* ------------------------------------------------------------------ *
 * Blocos
 * ------------------------------------------------------------------ */

function BarraPublica({ destino, rotuloEntrar }: { destino: string; rotuloEntrar: string }) {
  const [rolou, setRolou] = useState(false)

  useEffect(() => {
    const aoRolar = () => setRolou(window.scrollY > 12)
    aoRolar()
    window.addEventListener('scroll', aoRolar, { passive: true })
    return () => window.removeEventListener('scroll', aoRolar)
  }, [])

  return (
    <header
      className={cx(
        'sticky top-0 z-30 transition-colors duration-300',
        rolou && 'tk-glass border-x-0 border-t-0',
      )}
    >
      <div className="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3 sm:px-6">
        <Link to="/" className="flex shrink-0 items-center gap-2.5">
          <MarcaSimbolo tamanho={36} />
          <span className="font-display text-[15px] leading-none font-bold tracking-tight text-tinta-900">
            Track<span className="text-tinta-500">Wheel</span>
          </span>
        </Link>

        <nav className="ml-auto hidden items-center gap-1 md:flex">
          {SECOES.map((secao) => (
            <a
              key={secao.id}
              href={`#${secao.id}`}
              className="rounded-full px-3.5 py-2 text-sm font-semibold text-tinta-600 transition-colors hover:bg-white/[0.07] hover:text-tinta-900"
            >
              {secao.rotulo}
            </a>
          ))}
        </nav>

        <Link to={destino} className="ml-auto shrink-0 md:ml-2">
          <Botao>
            {rotuloEntrar}
            <ArrowRight size={16} weight="bold" />
          </Botao>
        </Link>
      </div>
    </header>
  )
}

/**
 * O palco da marca: a roda girando dentro de aneis de luz, com o letreiro em
 * pincel embaixo e um cartao de OS flutuando — a promessa e o produto juntos.
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
        <MarcaSimbolo tamanho={208} animado className="drop-shadow-[0_24px_60px_rgba(0,0,0,0.75)]" />
        <MarcaTexto className="w-44 opacity-95 sm:w-52" />

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
          <p className="truncate text-xs text-tinta-500">Mercedes-Benz Accelo 1016 · Radiador</p>
          <div className="mt-3 flex items-end justify-between gap-3 border-t border-white/8 pt-3">
            <span className="text-[10px] tracking-[0.16em] text-tinta-500 uppercase">No pátio há</span>
            <span className="font-mono text-lg font-bold tabular-nums text-tinta-900">02:14:37</span>
          </div>
        </div>
      </div>
    </div>
  )
}

function Heroi({ destino, rotuloEntrar }: { destino: string; rotuloEntrar: string }) {
  return (
    <section className="relative overflow-hidden">
      {/* Malha tecnica esmaecida nas bordas — a planta baixa do pátio */}
      <div className="tk-malha pointer-events-none absolute inset-0 [mask-image:radial-gradient(70%_60%_at_50%_25%,#000,transparent)]" />

      <div className="relative mx-auto grid max-w-6xl items-center gap-12 px-4 pt-10 pb-16 sm:px-6 sm:pt-16 sm:pb-24 lg:grid-cols-[1.05fr_1fr] lg:gap-8">
        <div>
          <span
            className="tk-entra inline-flex items-center gap-2 rounded-full border border-white/12 bg-white/[0.05] px-3.5 py-1.5 text-[11px] font-bold tracking-[0.18em] text-tinta-600 uppercase"
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
            O Track Wheel é o sistema de ordem de serviço que se adapta ao ramo da sua oficina. Quem
            conserta radiador vê campos de radiador; quem faz suspensão vê os dela. Cliente, veículo,
            peça e pagamento continuam sendo os mesmos de sempre.
          </p>

          <div className="tk-entra mt-8 flex flex-wrap gap-3" style={{ animationDelay: '280ms' }}>
            <Link to={destino}>
              <Botao tamanho="grande">
                {rotuloEntrar}
                <ArrowRight size={18} weight="bold" />
              </Botao>
            </Link>
            <a href="#diferencial">
              <Botao tamanho="grande" variante="secundario">
                Ver o diferencial
              </Botao>
            </a>
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

function FaixaNumeros() {
  return (
    <div className="border-y border-white/8 bg-white/[0.02]">
      <dl className="mx-auto grid max-w-6xl grid-cols-2 gap-px px-4 sm:px-6 lg:grid-cols-4">
        {NUMEROS.map((numero) => (
          <div key={numero.rotulo} className="py-8 sm:py-10">
            <dt className="font-display text-4xl font-bold tracking-tight text-tinta-900 sm:text-5xl">
              {numero.valor}
            </dt>
            <dd className="mt-1.5 max-w-[15rem] text-sm leading-snug text-tinta-500">
              {numero.rotulo}
            </dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

/** A prova do diferencial: troque o ramo e o formulario da OS troca junto. */
function Diferencial() {
  const [ramoAtivo, setRamoAtivo] = useState(RAMOS[0])

  return (
    <Secao
      id="diferencial"
      sobretitulo="O diferencial"
      titulo="Troque o ramo e a Ordem de Serviço troca com ele"
      descricao="O catálogo de campos vive no banco, versionado — não no código. Escolha um ramo aqui do lado e veja o formulário que a sua equipe encontraria ao abrir uma OS."
    >
      <div className="grid gap-6 lg:grid-cols-[minmax(0,20rem)_1fr] lg:gap-10">
        <div>
          <div className="flex flex-wrap gap-2">
            {RAMOS.map((ramo) => (
              <button
                key={ramo.valor}
                type="button"
                onClick={() => setRamoAtivo(ramo)}
                aria-pressed={ramoAtivo.valor === ramo.valor}
                className={cx(
                  'min-h-11 rounded-full border px-4 text-sm font-semibold transition-all',
                  ramoAtivo.valor === ramo.valor
                    ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                    : 'border-white/10 bg-white/[0.04] text-tinta-600 hover:border-white/25 hover:text-tinta-900',
                )}
              >
                {ramo.rotulo}
              </button>
            ))}
          </div>

          <p className="mt-6 text-sm leading-relaxed text-tinta-500">
            São <strong className="font-semibold text-tinta-800">{ramoAtivo.total} campos</strong> no
            catálogo de {ramoAtivo.rotulo.toLowerCase()} — aqui embaixo estão seis deles. Campos com
            um rastro claro só aparecem quando outro campo pede: perguntar o local do vazamento antes
            de saber se houve vazamento é ruído no balcão.
          </p>

          <ul className="mt-6 space-y-2 text-sm text-tinta-600">
            {[
              'Obrigatório de verdade: o backend recusa a OS sem ele',
              'Foto e assinatura são tipos de campo, não anexo solto',
              'Editar o catálogo cria uma versão — OS antiga não muda',
            ].map((item) => (
              <li key={item} className="flex items-start gap-2.5">
                <ListChecks size={16} className="mt-0.5 shrink-0 text-tinta-500" />
                {item}
              </li>
            ))}
          </ul>
        </div>

        {/* Simulacao do formulario — mesmo vocabulario visual do app real */}
        <div className="tk-card overflow-hidden p-4 sm:p-6">
          <div className="mb-5 flex items-center justify-between gap-3 border-b border-white/8 pb-4">
            <div className="min-w-0">
              <p className="font-display text-base font-bold tracking-tight text-tinta-900">
                Detalhes do serviço
              </p>
              <p className="truncate font-mono text-[11px] text-tinta-500">
                {ramoAtivo.valor} · schema v1
              </p>
            </div>
            <span className="shrink-0 rounded-full bg-white px-2.5 py-1 text-[11px] font-bold text-black">
              {ramoAtivo.total} campos
            </span>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            {ramoAtivo.campos.map((campo) => (
              <div
                key={campo.rotulo}
                className={cx(
                  'rounded-xl border p-3',
                  campo.condicional
                    ? 'border-dashed border-white/20 bg-white/[0.02]'
                    : 'border-white/10 bg-white/[0.035]',
                )}
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="text-[13px] font-semibold text-tinta-800">
                    {campo.rotulo}
                    {campo.obrigatorio && <span className="ml-1 text-tinta-500">*</span>}
                  </p>
                  <span className="shrink-0 rounded bg-white/[0.07] px-1.5 py-0.5 font-mono text-[10px] text-tinta-500">
                    {campo.tipo}
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-tinta-500">
                  {campo.grupo}
                  {campo.condicional && ' · só aparece se o anterior for sim'}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Secao>
  )
}

function Fluxo() {
  return (
    <Secao
      id="fluxo"
      sobretitulo="Fluxo da OS"
      titulo="Do orçamento à entrega, sem pular etapa"
      descricao="As transições válidas moram na própria regra de negócio: não dá para entregar um carro que nunca foi aprovado. Cada passagem grava quem fez e quando."
    >
      <ol className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {FLUXO.map((etapa, i) => (
          <li
            key={etapa.rotulo}
            className="tk-card tk-card-hover flex items-start gap-3.5 p-4 sm:p-5"
          >
            <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-white/[0.07] font-mono text-xs font-bold text-tinta-700 ring-1 ring-white/10">
              {String(i + 1).padStart(2, '0')}
            </span>
            <div className="min-w-0">
              <p className="font-display font-bold tracking-tight text-tinta-900">{etapa.rotulo}</p>
              <p className="mt-0.5 text-sm leading-snug text-tinta-500">{etapa.nota}</p>
            </div>
          </li>
        ))}
      </ol>
    </Secao>
  )
}

function Recursos() {
  return (
    <Secao
      id="recursos"
      sobretitulo="Recursos"
      titulo="O que a oficina faz no dia, registrado"
      descricao="Nada de módulo que ninguém abre. Cada tela existe porque alguém no balcão precisava dela."
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {RECURSOS.map(({ Icone, titulo, texto }) => (
          <article key={titulo} className="tk-card tk-card-hover flex flex-col gap-3 p-5 sm:p-6">
            <span className="flex size-11 items-center justify-center rounded-xl bg-white/[0.06] text-tinta-900 ring-1 ring-white/10">
              <Icone size={21} weight="duotone" />
            </span>
            <h3 className="font-display text-lg font-bold tracking-tight text-tinta-900">{titulo}</h3>
            <p className="text-sm leading-relaxed text-tinta-500">{texto}</p>
          </article>
        ))}
      </div>
    </Secao>
  )
}

function Ramos() {
  return (
    <Secao
      id="ramos"
      sobretitulo="Ramos atendidos"
      titulo="Nove catálogos prontos. E o décimo é o seu."
      descricao="A oficina nunca começa de uma tela em branco: no cadastro você escolhe o ramo e os campos já vêm semeados. Depois é só ajustar o que for diferente na sua casa."
    >
      <div className="flex flex-wrap gap-2.5">
        {RAMOS.map((ramo) => (
          <span
            key={ramo.valor}
            className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-4 py-2.5 text-sm font-semibold text-tinta-800"
          >
            {ramo.rotulo}
            <span className="font-mono text-[11px] text-tinta-500">{ramo.total}</span>
          </span>
        ))}
        {OUTROS_RAMOS.map((rotulo) => (
          <span
            key={rotulo}
            className="inline-flex items-center rounded-full border border-dashed border-white/15 px-4 py-2.5 text-sm font-semibold text-tinta-600"
          >
            {rotulo}
          </span>
        ))}
      </div>
    </Secao>
  )
}

function NoCelular() {
  return (
    <Secao
      sobretitulo="No box e no balcão"
      titulo="Feito para a mão suja de graxa"
      descricao="Alvos grandes, teclado certo para cada campo e a ação principal fixa no rodapé, no alcance do polegar. Instala na tela inicial e abre como aplicativo."
      acao={<PromptInstalar />}
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[
          {
            Icone: DeviceMobile,
            titulo: 'Instala como app',
            texto: 'PWA: vai para a tela inicial do celular, sem loja e sem instalador.',
          },
          {
            Icone: WhatsappLogo,
            titulo: 'Orçamento no WhatsApp',
            texto: 'Texto formatado com itens, total e garantia, pronto para enviar.',
          },
          {
            Icone: ShieldCheck,
            titulo: 'Cada oficina no seu canto',
            texto: 'O identificador da oficina sai sempre do token — nunca do que o app manda.',
          },
          {
            Icone: ListChecks,
            titulo: 'Papéis da equipe',
            texto: 'O mecânico só enxerga as OS atribuídas a ele. Dono vê tudo.',
          },
        ].map(({ Icone, titulo, texto }) => (
          <article key={titulo} className="tk-card flex flex-col gap-3 p-5">
            <Icone size={22} weight="duotone" className="text-tinta-700" />
            <h3 className="font-display font-bold tracking-tight text-tinta-900">{titulo}</h3>
            <p className="text-sm leading-relaxed text-tinta-500">{texto}</p>
          </article>
        ))}
      </div>
    </Secao>
  )
}

function ChamadaFinal({ destino, rotuloEntrar }: { destino: string; rotuloEntrar: string }) {
  const { alvo, visivel } = useRevelar<HTMLDivElement>()

  return (
    <div className="mx-auto w-full max-w-6xl px-4 pb-20 sm:px-6 sm:pb-28">
      <div
        ref={alvo}
        className={cx(
          'tk-hero flex flex-col items-center gap-6 px-6 py-14 text-center transition-all duration-700 sm:px-10 sm:py-20',
          visivel ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0',
        )}
      >
        <MarcaSimbolo tamanho={72} />
        <h2 className="max-w-2xl font-display text-3xl font-bold tracking-tight text-balance text-tinta-900 sm:text-[40px] sm:leading-tight">
          Abra a primeira OS ainda hoje
        </h2>
        <p className="max-w-xl text-base leading-relaxed text-tinta-600">
          Entre com a conta Google da oficina, escolha o ramo e o catálogo de campos vem semeado. Do
          cadastro à primeira ordem de serviço são poucos minutos.
        </p>
        <Link to={destino}>
          <Botao tamanho="grande">
            {rotuloEntrar}
            <ArrowRight size={18} weight="bold" />
          </Botao>
        </Link>
      </div>
    </div>
  )
}

function Rodape() {
  return (
    <footer className="border-t border-white/8">
      <div className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-10 sm:flex-row sm:items-start sm:justify-between sm:px-6">
        <div>
          <Link to="/" className="flex items-center gap-2.5">
            <MarcaSimbolo tamanho={34} />
            <span className="font-display text-[15px] leading-none font-bold tracking-tight text-tinta-900">
              Track<span className="text-tinta-500">Wheel</span>
            </span>
          </Link>
          <p className="mt-3 max-w-xs text-sm leading-relaxed text-tinta-500">
            Gestão para oficinas mecânicas, com a Ordem de Serviço que fala a língua do seu ramo.
          </p>
        </div>

        <nav className="flex flex-wrap gap-x-8 gap-y-3">
          {SECOES.map((secao) => (
            <a
              key={secao.id}
              href={`#${secao.id}`}
              className="text-sm font-semibold text-tinta-600 transition-colors hover:text-tinta-900"
            >
              {secao.rotulo}
            </a>
          ))}
          <Link
            to="/login"
            className="text-sm font-semibold text-tinta-600 transition-colors hover:text-tinta-900"
          >
            Entrar
          </Link>
        </nav>
      </div>

      <div className="mx-auto max-w-6xl px-4 pb-10 sm:px-6">
        <p className="border-t border-white/8 pt-6 text-xs text-tinta-500">
          © {new Date().getFullYear()} Track Wheel · Dados tratados conforme a LGPD
        </p>
      </div>
    </footer>
  )
}

/* ------------------------------------------------------------------ *
 * Pagina
 * ------------------------------------------------------------------ */

export function PaginaHome() {
  const { sessao, modoDev, usuarioFirebase } = useAuth()

  // Quem ja esta logado nao precisa passar pela tela de login de novo.
  const logado = modoDev || !!usuarioFirebase
  const destino = logado && !sessao?.precisaOnboarding ? '/app' : '/login'
  const rotuloEntrar = logado && !sessao?.precisaOnboarding ? 'Abrir painel' : 'Entrar'

  return (
    <div className="flex min-h-full flex-col">
      <BarraPublica destino={destino} rotuloEntrar={rotuloEntrar} />
      <main className="flex-1">
        <Heroi destino={destino} rotuloEntrar={rotuloEntrar} />
        <FaixaNumeros />
        <Diferencial />
        <Fluxo />
        <Recursos />
        <Ramos />
        <NoCelular />
        <ChamadaFinal destino={destino} rotuloEntrar={rotuloEntrar} />
      </main>
      <Rodape />
    </div>
  )
}
