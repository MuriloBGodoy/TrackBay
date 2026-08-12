import { useId } from 'react'
import wordmark from '../assets/wordmark-trackwheel.png'
import { cx } from './ui'

/**
 * ===== Marca Track Wheel =====
 *
 * O logo original e uma arte em pincel: pneu em perspectiva, raios de liga e o
 * monograma "TW" pintado por cima. Aqui ele vira duas pecas independentes:
 *
 * - `MarcaSimbolo` — o pneu visto de frente, redesenhado em vetor. Precisa
 *   funcionar a 20px na barra lateral e a 240px no heroi, entao a geometria e
 *   limpa: banda de rodagem, aro prata, cinco raios discretos e o TW por cima.
 *   Monocromatico por natureza — nenhuma cor, so luz.
 * - `MarcaTexto` — o letreiro em pincel recortado da propria arte do dono e
 *   passado para branco com transparencia, entao ele mantem a textura da
 *   pincelada sobre o grafite.
 */

export function MarcaSimbolo({
  tamanho = 32,
  className,
  animado = false,
}: {
  tamanho?: number
  className?: string
  /** Gira o aro lentamente — usado so no heroi da landing. */
  animado?: boolean
}) {
  // Gradientes precisam de id unico: dois simbolos na mesma pagina senao
  // compartilham a definicao e o segundo herda a escala do primeiro.
  const id = useId().replace(/:/g, '')

  return (
    <svg
      viewBox="0 0 64 64"
      width={tamanho}
      height={tamanho}
      className={cx('shrink-0', className)}
      role="img"
      aria-label="Track Wheel"
    >
      <defs>
        <radialGradient id={`${id}-aro`} cx="38%" cy="32%" r="72%">
          <stop offset="0%" stopColor="#ffffff" />
          <stop offset="45%" stopColor="#c9c9d0" />
          <stop offset="100%" stopColor="#5b5b64" />
        </radialGradient>
        <linearGradient id={`${id}-pneu`} x1="0.15" y1="0" x2="0.85" y2="1">
          <stop offset="0%" stopColor="#3a3a42" />
          <stop offset="55%" stopColor="#1d1d21" />
          <stop offset="100%" stopColor="#0d0d10" />
        </linearGradient>
      </defs>

      {/* Pneu */}
      <circle cx="32" cy="32" r="31" fill={`url(#${id}-pneu)`} />
      {/* Banda de rodagem: o tracejado desenha os blocos do desenho do pneu. */}
      <circle
        cx="32"
        cy="32"
        r="27.4"
        fill="none"
        stroke="#08080a"
        strokeWidth="4.4"
        strokeDasharray="3.4 3.6"
        opacity="0.75"
      />
      <circle cx="32" cy="32" r="24.6" fill="none" stroke="rgba(255,255,255,0.10)" strokeWidth="1" />

      {/* Aro de liga */}
      <g className={animado ? 'tk-gira' : undefined} style={{ transformOrigin: '32px 32px' }}>
        <circle cx="32" cy="32" r="22.6" fill={`url(#${id}-aro)`} />
        <circle cx="32" cy="32" r="19.8" fill="#15151a" />
        {/* Cinco raios, discretos: a 20px eles viram textura, nao ruido. */}
        <g fill={`url(#${id}-aro)`} opacity="0.34">
          {[0, 72, 144, 216, 288].map((angulo) => (
            <path
              key={angulo}
              d="M32 26.5 L36.4 13.4 Q32 11.6 27.6 13.4 Z"
              transform={`rotate(${angulo} 32 32)`}
            />
          ))}
        </g>
        <circle cx="32" cy="32" r="3.6" fill={`url(#${id}-aro)`} opacity="0.5" />
      </g>

      {/* Monograma: branco com contorno escuro, como na arte pintada. */}
      <text
        x="32"
        y="33.4"
        textAnchor="middle"
        dominantBaseline="central"
        fontFamily="'Space Grotesk', 'Segoe UI', sans-serif"
        fontSize="21"
        fontWeight="700"
        letterSpacing="-2"
        fill="#ffffff"
        stroke="#0b0b0e"
        strokeWidth="2.6"
        paintOrder="stroke"
        transform="skewX(-9) translate(5.3 0)"
      >
        TW
      </text>
    </svg>
  )
}

/** O letreiro em pincel, em duas linhas, como no logo original. */
export function MarcaTexto({ className }: { className?: string }) {
  return <img src={wordmark} alt="Track Wheel" className={cx('select-none', className)} />
}

/**
 * Assinatura horizontal: simbolo + nome tipografico. E o que vai na barra
 * lateral e no cabecalho — o letreiro em pincel e alto demais para uma barra.
 */
export function MarcaHorizontal({
  tamanho = 32,
  className,
  ocultarTexto = false,
}: {
  tamanho?: number
  className?: string
  ocultarTexto?: boolean
}) {
  return (
    <span className={cx('flex items-center gap-2.5', className)}>
      <MarcaSimbolo tamanho={tamanho} />
      {!ocultarTexto && (
        <span className="font-display text-[15px] leading-none font-bold tracking-tight whitespace-nowrap text-tinta-900">
          Track<span className="text-tinta-500">Wheel</span>
        </span>
      )}
    </span>
  )
}
