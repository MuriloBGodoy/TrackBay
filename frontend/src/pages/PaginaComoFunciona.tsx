import { useState } from 'react'
import { DeviceMobile, ListChecks, ShieldCheck, WhatsappLogo } from '@phosphor-icons/react'
import { PromptInstalar } from '../components/PromptInstalar'
import { BarraPublica, ChamadaFinal, Rodape, Secao } from '../components/site'
import { cx } from '../components/ui'

/**
 * ===== Como funciona =====
 *
 * O aprofundamento que saiu da home: a simulacao de campos por ramo, o fluxo da
 * OS, o catalogo de ramos, os numeros e o comportamento no celular. Quem chega
 * aqui ja clicou para se aprofundar — pode ser denso.
 */

type RamoVitrine = {
  valor: string
  rotulo: string
  total: number
  campos: {
    rotulo: string
    tipo: string
    grupo: string
    obrigatorio?: boolean
    /** Campo que so aparece quando outro tem certo valor. */
    condicional?: boolean
  }[]
}

/**
 * Amostra real do catalogo de campos que o backend semeia por ramo
 * (domain/seed/TemplatesPadrao.java). E o argumento de venda inteiro: a mesma
 * OS muda de cara conforme a oficina. O `total` e a contagem real do template —
 * se mexer nos seeds, confira aqui.
 */
const RAMOS: RamoVitrine[] = [
  {
    valor: 'MECANICA_GERAL',
    rotulo: 'Mecânica geral',
    total: 8,
    campos: [
      { rotulo: 'Sistema afetado', tipo: 'MULTI_SELECT', grupo: 'Diagnóstico', obrigatorio: true },
      { rotulo: 'Ruídos observados', tipo: 'MULTI_SELECT', grupo: 'Diagnóstico' },
      { rotulo: 'Quando ocorre', tipo: 'SELECT', grupo: 'Diagnóstico' },
      { rotulo: 'Revisão preventiva', tipo: 'CHECKLIST', grupo: 'Preventiva' },
      { rotulo: 'Fluidos trocados', tipo: 'MULTI_SELECT', grupo: 'Serviço' },
      { rotulo: 'Km da próxima revisão', tipo: 'NUMERO', grupo: 'Serviço' },
    ],
  },
  {
    valor: 'SUSPENSAO_FREIOS',
    rotulo: 'Suspensão e freios',
    total: 8,
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
    total: 8,
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
  {
    valor: 'FUNILARIA_PINTURA',
    rotulo: 'Funilaria e pintura',
    total: 11,
    campos: [
      { rotulo: 'Peças afetadas', tipo: 'MULTI_SELECT', grupo: 'Avaliação', obrigatorio: true },
      { rotulo: 'Tipo de dano', tipo: 'SELECT', grupo: 'Avaliação', obrigatorio: true },
      { rotulo: 'Código da tinta', tipo: 'TEXTO', grupo: 'Pintura' },
      { rotulo: 'É sinistro?', tipo: 'BOOLEANO', grupo: 'Sinistro' },
      { rotulo: 'Seguradora', tipo: 'TEXTO', grupo: 'Sinistro', condicional: true },
      { rotulo: 'Foto antes', tipo: 'FOTO', grupo: 'Evidências' },
    ],
  },
]

const OUTROS_RAMOS = ['Radiador', 'Elétrica automotiva', 'Outro (você monta o seu)']

const FLUXO = [
  { rotulo: 'Orçamento', nota: 'sai em PDF com linha de assinatura' },
  { rotulo: 'Aprovada', nota: 'grava a data do aceite' },
  { rotulo: 'Em execução', nota: 'cronômetro correndo no pátio' },
  { rotulo: 'Aguardando peça', nota: 'volta para execução quando chegar' },
  { rotulo: 'Pronta', nota: 'cliente avisado pelo WhatsApp' },
  { rotulo: 'Entregue', nota: 'baixa o estoque das peças próprias' },
]

const NUMEROS = [
  { valor: '9', rotulo: 'ramos com o catálogo já pronto' },
  { valor: '12', rotulo: 'tipos de campo, da foto à assinatura' },
  { valor: '0', rotulo: 'linha de código para mudar a sua ficha' },
  { valor: '100%', rotulo: 'em português, feito para o Brasil' },
]

const NO_CELULAR = [
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
]

function Cabecalho() {
  return (
    <section className="relative overflow-hidden">
      <div className="tk-malha pointer-events-none absolute inset-0" />
      <div className="relative mx-auto max-w-6xl px-4 pt-12 pb-4 sm:px-6 sm:pt-20 sm:pb-8">
        <p className="mb-3 text-[11px] font-bold tracking-[0.24em] text-tinta-500 uppercase">
          Como funciona
        </p>
        <h1 className="max-w-3xl font-display text-[34px] leading-[1.08] font-bold tracking-tight text-balance text-tinta-900 sm:text-[52px]">
          A mesma OS, com a cara do seu ramo.
        </h1>
        <p className="mt-5 max-w-xl text-[17px] leading-relaxed text-tinta-600">
          Por dentro: o catálogo de campos que troca com o ramo, o caminho que a ordem de serviço
          percorre e o que muda quando o app está na mão de quem está no box.
        </p>
      </div>
    </section>
  )
}

/** A prova do diferencial: troque o ramo e o formulario da OS troca junto. */
function Diferencial() {
  const [ramoAtivo, setRamoAtivo] = useState(RAMOS[0])

  return (
    <Secao
      id="diferencial"
      sobretitulo="O diferencial"
      titulo="Troque o ramo e o formulário troca junto"
      descricao="O schema fica no banco, versionado. Nenhum destes campos está escrito no código do app — e é por isso que a sua oficina pode ter os seus."
    >
      <div className="mb-8 flex flex-wrap gap-2">
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

      <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr]">
        <div>
          <p className="text-base leading-relaxed text-tinta-600">
            São <strong className="font-semibold text-tinta-800">{ramoAtivo.total} campos</strong> no
            catálogo de {ramoAtivo.rotulo.toLowerCase()} — aqui embaixo estão seis deles. Campos com
            um rastro claro só aparecem quando outro campo pede: perguntar a seguradora antes de
            saber se o serviço é sinistro é ruído no balcão.
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
      id="celular"
      sobretitulo="No box e no balcão"
      titulo="Feito para a mão suja de graxa"
      descricao="Alvos grandes, teclado certo para cada campo e a ação principal fixa no rodapé, no alcance do polegar. Instala na tela inicial e abre como aplicativo."
      acao={<PromptInstalar />}
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {NO_CELULAR.map(({ Icone, titulo, texto }) => (
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

export function PaginaComoFunciona() {
  return (
    <div className="flex min-h-full flex-col">
      <BarraPublica />
      <main className="flex-1">
        <Cabecalho />
        <Diferencial />
        <FaixaNumeros />
        <Fluxo />
        <Ramos />
        <NoCelular />
        <ChamadaFinal />
      </main>
      <Rodape />
    </div>
  )
}
