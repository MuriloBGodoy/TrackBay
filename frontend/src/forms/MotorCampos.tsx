import { useMemo, useState } from 'react'
import type { CampoDinamico, TipoVeiculo } from '../types/dominio'
import { arquivosApi } from '../api/recursos'
import { AreaTexto, Campo, Input, Selecao, cx } from '../components/ui'
import { data, moeda } from '../lib/formato'

type Valores = Record<string, unknown>

interface Props {
  campos: CampoDinamico[]
  valores: Valores
  aoMudar: (chave: string, valor: unknown) => void
  tipoVeiculo?: TipoVeiculo
  erros?: Record<string, string>
  somenteLeitura?: boolean
}

/** Um campo condicional so aparece quando o campo de que depende tem o valor esperado. */
function condicaoSatisfeita(campo: CampoDinamico, valores: Valores): boolean {
  if (!campo.condicional) return true
  const atual = valores[campo.condicional.campo]
  return String(atual) === String(campo.condicional.valor)
}

/**
 * Renderiza o formulario a partir do schema vindo do banco.
 * Nenhum campo e hardcoded: mudou o template, muda a tela.
 */
export function MotorCampos({
  campos,
  valores,
  aoMudar,
  tipoVeiculo,
  erros = {},
  somenteLeitura = false,
}: Props) {
  const visiveis = useMemo(
    () =>
      campos
        .filter((c) => !c.aplicavelA?.length || !tipoVeiculo || c.aplicavelA.includes(tipoVeiculo))
        .filter((c) => condicaoSatisfeita(c, valores))
        // Na leitura, campo sem resposta nao aparece — nem ele nem o grupo dele.
        .filter((c) => !somenteLeitura || !estaVazio(valores[c.chave]))
        .sort((a, b) => a.ordem - b.ordem),
    [campos, valores, tipoVeiculo, somenteLeitura],
  )

  const grupos = useMemo(() => {
    const mapa = new Map<string, CampoDinamico[]>()
    for (const campo of visiveis) {
      const grupo = campo.grupo || 'Geral'
      const lista = mapa.get(grupo) ?? []
      lista.push(campo)
      mapa.set(grupo, lista)
    }
    return [...mapa.entries()]
  }, [visiveis])

  if (!campos.length) {
    return <p className="text-sm text-tinta-500">Nenhum campo configurado para este ramo.</p>
  }

  if (somenteLeitura && !visiveis.length) {
    return <p className="text-sm text-tinta-500">Nenhum campo do ramo foi preenchido nesta OS.</p>
  }

  // Em leitura os campos viram uma lista de definicoes (rotulo -> resposta).
  const Envoltorio = somenteLeitura ? 'dl' : 'div'

  return (
    <div className="space-y-6">
      {grupos.map(([grupo, camposDoGrupo]) => (
        <section key={grupo} className={somenteLeitura ? 'space-y-2' : 'space-y-4'}>
          <h3 className="text-xs font-bold tracking-wide text-tinta-500 uppercase">{grupo}</h3>
          <Envoltorio className={somenteLeitura ? undefined : 'space-y-4'}>
            {camposDoGrupo.map((campo) => (
              <CampoDinamicoRender
                key={campo.chave}
                campo={campo}
                valor={valores[campo.chave]}
                aoMudar={(v) => aoMudar(campo.chave, v)}
                erro={erros[campo.chave]}
                somenteLeitura={somenteLeitura}
              />
            ))}
          </Envoltorio>
        </section>
      ))}
    </div>
  )
}

/** Um campo sem resposta nao vira linha em branco na leitura — ele some. */
function estaVazio(valor: unknown): boolean {
  return (
    valor === null ||
    valor === undefined ||
    (typeof valor === 'string' && !valor.trim()) ||
    (Array.isArray(valor) && !valor.length)
  )
}

function CampoDinamicoRender({
  campo,
  valor,
  aoMudar,
  erro,
  somenteLeitura,
}: {
  campo: CampoDinamico
  valor: unknown
  aoMudar: (valor: unknown) => void
  erro?: string
  somenteLeitura: boolean
}) {
  const rotulo = campo.unidade ? `${campo.rotulo}` : campo.rotulo

  /*
   * Em leitura a OS e um documento, nao um formulario: mostrar "Selecione…"
   * num campo que ninguem preencheu faz o mecanico achar que ainda da para
   * editar. Entao o modo leitura tem seu proprio desenho — rotulo e resposta.
   */
  if (somenteLeitura) {
    if (estaVazio(valor)) return null
    if (campo.tipo === 'FOTO') {
      return (
        <CampoFoto campo={campo} valor={valor} aoMudar={aoMudar} erro={erro} somenteLeitura />
      )
    }
    return <LinhaLeitura campo={campo} valor={valor} />
  }

  switch (campo.tipo) {
    case 'TEXTO_LONGO':
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <AreaTexto
            rows={3}
            erro={!!erro}
            value={(valor as string) ?? ''}
            placeholder={campo.placeholder}
            disabled={somenteLeitura}
            onChange={(e) => aoMudar(e.target.value)}
          />
        </Campo>
      )

    case 'NUMERO':
    case 'DECIMAL':
    case 'MOEDA':
      return (
        <Campo
          rotulo={rotulo}
          erro={erro}
          obrigatorio={campo.obrigatorio}
          dica={campo.tipo === 'MOEDA' && valor ? moeda(Number(valor)) : campo.unidade}
        >
          <div className="relative">
            <Input
              // inputMode decimal abre o teclado numerico no celular.
              type="number"
              inputMode={campo.tipo === 'NUMERO' ? 'numeric' : 'decimal'}
              step={campo.tipo === 'NUMERO' ? '1' : '0.01'}
              value={(valor as number) ?? ''}
              placeholder={campo.placeholder}
              disabled={somenteLeitura}
              erro={!!erro}
              onChange={(e) => aoMudar(e.target.value === '' ? null : Number(e.target.value))}
            />
            {campo.unidade && (
              <span className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-sm text-tinta-400">
                {campo.unidade}
              </span>
            )}
          </div>
        </Campo>
      )

    case 'DATA':
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <Input
            type="date"
            value={(valor as string) ?? ''}
            disabled={somenteLeitura}
            erro={!!erro}
            onChange={(e) => aoMudar(e.target.value)}
          />
        </Campo>
      )

    case 'BOOLEANO':
      return (
        <div className="flex min-h-11 items-center justify-between gap-3">
          <span className="text-sm font-medium text-tinta-800">
            {rotulo}
            {campo.obrigatorio && <span className="ml-0.5 text-red-400">*</span>}
          </span>
          <button
            type="button"
            role="switch"
            aria-checked={!!valor}
            aria-label={rotulo}
            disabled={somenteLeitura}
            onClick={() => aoMudar(!valor)}
            className={cx(
              'relative h-7 w-12 shrink-0 rounded-full border transition-colors',
              valor
                ? 'border-white/70 bg-white shadow-[var(--tk-glow)]'
                : 'border-white/12 bg-white/[0.07]',
              somenteLeitura && 'opacity-60',
            )}
          >
            <span
              className={cx(
                'absolute top-1 size-5 rounded-full transition-transform',
                valor ? 'translate-x-6 bg-black' : 'translate-x-1 bg-tinta-500',
              )}
            />
          </button>
        </div>
      )

    case 'SELECT':
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <Selecao
            erro={!!erro}
            value={(valor as string) ?? ''}
            disabled={somenteLeitura}
            onChange={(e) => aoMudar(e.target.value || null)}
          >
            <option value="">Selecione…</option>
            {campo.opcoes.map((opcao) => (
              <option key={opcao} value={opcao}>
                {opcao}
              </option>
            ))}
          </Selecao>
        </Campo>
      )

    case 'MULTI_SELECT':
    case 'CHECKLIST': {
      const selecionados = Array.isArray(valor) ? (valor as string[]) : []
      const alternar = (opcao: string) => {
        aoMudar(
          selecionados.includes(opcao)
            ? selecionados.filter((s) => s !== opcao)
            : [...selecionados, opcao],
        )
      }
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <div className="flex flex-wrap gap-2">
            {campo.opcoes.map((opcao) => {
              const ativo = selecionados.includes(opcao)
              return (
                <button
                  key={opcao}
                  type="button"
                  aria-pressed={ativo}
                  disabled={somenteLeitura}
                  onClick={() => alternar(opcao)}
                  className={cx(
                    'min-h-11 rounded-xl border px-3.5 text-sm font-semibold transition-all',
                    ativo
                      ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                      : 'border-white/10 bg-white/[0.04] text-tinta-600 hover:border-white/25 hover:text-tinta-900',
                    somenteLeitura && 'pointer-events-none opacity-70',
                  )}
                >
                  {opcao}
                </button>
              )
            })}
          </div>
        </Campo>
      )
    }

    case 'FOTO':
      return (
        <CampoFoto
          campo={campo}
          valor={valor}
          aoMudar={aoMudar}
          erro={erro}
          somenteLeitura={somenteLeitura}
        />
      )

    case 'ASSINATURA':
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <div className="flex h-28 items-center justify-center rounded-xl border-2 border-dashed border-white/15 bg-white/[0.02] text-sm text-tinta-500">
            {valor ? 'Assinado' : 'Toque para assinar'}
          </div>
        </Campo>
      )

    default:
      return (
        <Campo rotulo={rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
          <Input
            value={(valor as string) ?? ''}
            placeholder={campo.placeholder}
            disabled={somenteLeitura}
            erro={!!erro}
            onChange={(e) => aoMudar(e.target.value)}
          />
        </Campo>
      )
  }
}

/** Rotulo e resposta, do jeito que sai no papel. */
function LinhaLeitura({ campo, valor }: { campo: CampoDinamico; valor: unknown }) {
  const conteudo = () => {
    switch (campo.tipo) {
      case 'BOOLEANO':
        return valor ? 'Sim' : 'Não'
      case 'MOEDA':
        return moeda(Number(valor))
      case 'DATA':
        return data(String(valor))
      case 'MULTI_SELECT':
      case 'CHECKLIST':
        return (
          <span className="flex flex-wrap gap-1.5">
            {(valor as string[]).map((item) => (
              <span
                key={item}
                className="rounded-full border border-white/12 bg-white/[0.06] px-2.5 py-0.5 text-xs font-semibold text-tinta-800"
              >
                {item}
              </span>
            ))}
          </span>
        )
      case 'NUMERO':
      case 'DECIMAL':
        return `${Number(valor).toLocaleString('pt-BR')}${campo.unidade ? ` ${campo.unidade}` : ''}`
      default:
        return String(valor)
    }
  }

  return (
    <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1 border-b border-white/6 py-2 last:border-0">
      <dt className="text-sm text-tinta-500">{campo.rotulo}</dt>
      <dd
        className={cx(
          'text-sm font-semibold text-tinta-900',
          campo.tipo === 'TEXTO_LONGO' && 'w-full font-normal text-tinta-700',
        )}
      >
        {conteudo()}
      </dd>
    </div>
  )
}

/** OS antigas guardaram so o nome do arquivo; so da para exibir o que ja e URL. */
function ehUrl(valor: unknown): valor is string {
  return typeof valor === 'string' && (valor.startsWith('/') || valor.startsWith('http'))
}

/**
 * Foto do campo dinamico. Sobe a imagem na hora e guarda a URL — o campo nunca
 * segura o File: quando a OS for salva, o arquivo ja esta no storage.
 */
function CampoFoto({
  campo,
  valor,
  aoMudar,
  erro,
  somenteLeitura,
}: {
  campo: CampoDinamico
  valor: unknown
  aoMudar: (valor: unknown) => void
  erro?: string
  somenteLeitura: boolean
}) {
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState<string>()

  const enviar = async (arquivo?: File) => {
    if (!arquivo) return
    setEnviando(true)
    setFalha(undefined)
    try {
      const { url } = await arquivosApi.enviar(arquivo)
      aoMudar(url)
    } catch (e) {
      setFalha((e as Error).message)
    } finally {
      setEnviando(false)
    }
  }

  if (somenteLeitura) {
    return (
      <Campo rotulo={campo.rotulo} erro={erro} obrigatorio={campo.obrigatorio}>
        {ehUrl(valor) ? (
          <a href={valor} target="_blank" rel="noreferrer">
            <img
              src={valor}
              alt={campo.rotulo}
              className="max-h-48 rounded-xl border border-white/10 object-cover"
            />
          </a>
        ) : (
          <p className="text-sm text-tinta-500">{(valor as string) || '—'}</p>
        )}
      </Campo>
    )
  }

  return (
    <Campo
      rotulo={campo.rotulo}
      erro={erro ?? falha}
      obrigatorio={campo.obrigatorio}
      dica={enviando ? 'Enviando…' : 'Toque para usar a câmera'}
    >
      <div className="space-y-2">
        {ehUrl(valor) && (
          <img
            src={valor}
            alt={campo.rotulo}
            className="max-h-48 rounded-xl border border-tinta-200 object-cover"
          />
        )}
        <Input
          type="file"
          accept="image/*"
          // capture faz o celular abrir a camera direto, sem passar pela galeria.
          capture="environment"
          disabled={enviando}
          className="py-2 file:mr-3 file:rounded-lg file:border-0 file:bg-white/[0.09] file:px-3 file:py-2 file:text-sm file:font-semibold file:text-tinta-900"
          onChange={(e) => void enviar(e.target.files?.[0])}
        />
        {ehUrl(valor) && (
          <button
            type="button"
            className="text-sm font-semibold text-red-400"
            onClick={() => aoMudar(null)}
          >
            remover foto
          </button>
        )}
      </div>
    </Campo>
  )
}

/** Valida o preenchimento contra o schema — o mesmo contrato que o backend cobra. */
export function validarCampos(
  campos: CampoDinamico[],
  valores: Valores,
  tipoVeiculo?: TipoVeiculo,
): Record<string, string> {
  const erros: Record<string, string> = {}
  for (const campo of campos) {
    if (!campo.obrigatorio) continue
    if (campo.aplicavelA?.length && tipoVeiculo && !campo.aplicavelA.includes(tipoVeiculo)) continue
    if (!condicaoSatisfeita(campo, valores)) continue

    const valor = valores[campo.chave]
    const vazio =
      valor === null ||
      valor === undefined ||
      (typeof valor === 'string' && !valor.trim()) ||
      (Array.isArray(valor) && !valor.length)
    if (vazio) {
      erros[campo.chave] = 'Campo obrigatório'
    }
  }
  return erros
}
