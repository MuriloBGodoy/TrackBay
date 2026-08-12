import type { EChartsOption } from 'echarts'

/*
 * Tema dos graficos, separado do componente de proposito: quem monta a `option`
 * importa so daqui, e o ECharts (mais de 300 KB) continua fora do bundle ate o
 * painel realmente aparecer na tela. `import type` some na compilacao.
 */

/**
 * A aplicacao e monocromatica de proposito: a identidade de cada barra vem do
 * rotulo no eixo, nunca da cor — assim ninguem depende de distinguir tons de
 * cinza para ler o dado.
 */
export const TINTA = {
  marca: '#ffffff',
  grade: 'rgba(255,255,255,0.06)',
  rotulo: '#8b8b94',
  texto: '#f5f5f7',
  barra: 'rgba(255,255,255,0.82)',
  barraFraca: 'rgba(255,255,255,0.16)',
}

export const FONTE = 'Manrope, ui-sans-serif, system-ui, sans-serif'
export const FONTE_MONO = "'JetBrains Mono', ui-monospace, monospace"

/** Tooltip de vidro fosco, no mesmo material do chrome flutuante do app. */
export const TOOLTIP_BASE: EChartsOption['tooltip'] = {
  backgroundColor: 'rgba(18,18,21,0.94)',
  borderColor: 'rgba(255,255,255,0.12)',
  borderWidth: 1,
  padding: [8, 12],
  textStyle: { color: TINTA.texto, fontFamily: FONTE, fontSize: 12 },
  extraCssText: 'border-radius:12px;box-shadow:0 12px 32px rgba(0,0,0,0.5);',
}

export const EIXO_ROTULO = {
  color: TINTA.rotulo,
  fontSize: 11,
  fontFamily: FONTE_MONO,
}
