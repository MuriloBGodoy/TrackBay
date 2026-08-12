import { useEffect, useRef } from 'react'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsType } from 'echarts/core'
import type { EChartsOption } from 'echarts'

/*
 * So o que os graficos do painel usam entra no bundle: linha, barra, grade e
 * tooltip. Importar 'echarts' inteiro custaria mais de 1 MB num app que roda no
 * celular do mecanico com sinal ruim.
 *
 * As cores e fontes ficam em temaGrafico.ts para que montar a `option` nao
 * arraste esta biblioteca junto — este arquivo entra por React.lazy.
 */
echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, CanvasRenderer])

export default function Grafico({
  opcao,
  altura = 220,
  className,
  rotulo,
}: {
  opcao: EChartsOption
  altura?: number
  className?: string
  /** Descricao para quem usa leitor de tela — o canvas nao diz nada sozinho. */
  rotulo: string
}) {
  const alvo = useRef<HTMLDivElement>(null)
  const instancia = useRef<EChartsType | null>(null)

  useEffect(() => {
    if (!alvo.current) return
    const grafico = echarts.init(alvo.current, undefined, { renderer: 'canvas' })
    instancia.current = grafico

    const observador = new ResizeObserver(() => grafico.resize())
    observador.observe(alvo.current)

    return () => {
      observador.disconnect()
      grafico.dispose()
      instancia.current = null
    }
  }, [])

  useEffect(() => {
    // notMerge: series que somem precisam sumir de verdade ao trocar de filtro.
    instancia.current?.setOption(opcao, { notMerge: true })
  }, [opcao])

  return (
    <div
      ref={alvo}
      role="img"
      aria-label={rotulo}
      style={{ height: altura }}
      className={className}
    />
  )
}
