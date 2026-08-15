import { useId } from 'react'
import arte from '../assets/logo-trackbay.webp'
import { cx } from './ui'

/**
 * ===== Marca Track Bay =====
 *
 * Duas pecas, cada uma para um tamanho:
 *
 * - `MarcaArte` — a arte oficial (chaves cruzadas, pneu e o letreiro). Vai onde
 *   ha espaco de sobra: heroi da landing e painel do login. E raster e cheia de
 *   detalhe, entao nao desce abaixo de ~150px de largura.
 * - `MarcaSimbolo` — o portico da baia, vetorial. E o que sobrevive a 32px: barra
 *   lateral, cabecalho, favicon e icone do PWA. O nome e o desenho dizem a mesma
 *   coisa: a *baia*, o box onde o carro entra para ser atendido. Nada gira.
 *
 * A arte original (`trackbaylogo.png`, na raiz) e escura sobre cinza claro. O
 * asset daqui e ela recortada, com o fundo removido e a **luminancia invertida** —
 * sem inverter, o letreiro quase preto sumiria no grafite.
 *
 * A geometria do simbolo vive tambem em `public/icone.svg` (favicon e PWA) — se
 * um mudar, mude o outro. O SVG publico e deliberadamente sem texto: os PNGs do
 * manifesto sao renderizados a partir dele, fora do navegador, onde webfont nao
 * carrega.
 */

/** A arte oficial. Escala pela largura — o chamador passa `w-72`, `w-52`... */
export function MarcaArte({ className }: { className?: string }) {
  return (
    <img
      src={arte}
      alt="Track Bay"
      className={cx('block h-auto select-none', className)}
      draggable={false}
    />
  )
}

export function MarcaSimbolo({
  tamanho = 32,
  className,
}: {
  tamanho?: number
  className?: string
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
      aria-label="Track Bay"
    >
      <defs>
        <linearGradient id={`${id}-placa`} x1="0.1" y1="0" x2="0.9" y2="1">
          <stop offset="0%" stopColor="#26262d" />
          <stop offset="55%" stopColor="#17171b" />
          <stop offset="100%" stopColor="#0d0d10" />
        </linearGradient>
        <radialGradient id={`${id}-metal`} cx="36%" cy="22%" r="78%">
          <stop offset="0%" stopColor="#ffffff" />
          <stop offset="45%" stopColor="#c9c9d0" />
          <stop offset="100%" stopColor="#5b5b64" />
        </radialGradient>
        <linearGradient id={`${id}-carro`} x1="0.2" y1="0" x2="0.8" y2="1">
          <stop offset="0%" stopColor="#6a6a76" />
          <stop offset="100%" stopColor="#30303a" />
        </linearGradient>
        {/* O piso acende no meio e some nas pontas — o facho do box. */}
        <linearGradient id={`${id}-piso`} x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stopColor="#ffffff" stopOpacity="0" />
          <stop offset="50%" stopColor="#ffffff" stopOpacity="0.85" />
          <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
        </linearGradient>
      </defs>

      {/* Placa: da presenca em qualquer fundo e resolve o contraste do favicon. */}
      <rect x="1" y="1" width="62" height="62" rx="15" fill={`url(#${id}-placa)`} />
      <rect
        x="1"
        y="1"
        width="62"
        height="62"
        rx="15"
        fill="none"
        stroke="rgba(255,255,255,0.14)"
        strokeWidth="1"
      />

      {/* Vao escuro: o fundo do box, atras de tudo que esta dentro dele. */}
      <path
        d="M15.2 50 L15.2 28 Q15.2 20.2 23 20.2 L41 20.2 Q48.8 20.2 48.8 28 L48.8 50 Z"
        fill="#08080a"
      />

      {/* O veiculo, de frente. A cabine e trapezio, nao retangulo: e o que faz
          ler carro em vez de caixa. As rodas tocam o piso; o corpo cobre o topo
          delas, entao so o pneu aparece embaixo. */}
      <g fill="#2e2e37">
        <rect x="19.8" y="43.5" width="6" height="6.5" rx="1.6" />
        <rect x="38.2" y="43.5" width="6" height="6.5" rx="1.6" />
      </g>
      <path
        d="M22.8 39 L25.4 31 Q26 29.2 28 29.2 L36 29.2 Q38 29.2 38.6 31 L41.2 39 Z"
        fill={`url(#${id}-carro)`}
      />
      <path
        d="M25 37.2 L26.9 31.9 Q27.2 31 28.3 31 L35.7 31 Q36.8 31 37.1 31.9 L39 37.2 Z"
        fill="#0d0d12"
      />
      <rect x="18.5" y="37.5" width="27" height="9" rx="3" fill={`url(#${id}-carro)`} />
      <rect x="21.5" y="43.4" width="21" height="2" rx="1" fill="#16161c" opacity="0.75" />
      <g fill="#ffffff" opacity="0.92">
        <rect x="20.5" y="39.8" width="5.2" height="2.6" rx="1.3" />
        <rect x="38.3" y="39.8" width="5.2" height="2.6" rx="1.3" />
      </g>

      {/* Portico: moldura de metal, desenhada como recorte (evenodd). Ombro
          arredondado em vez de meia-lua — porta de box, nao arco romano. */}
      <path
        d="M10 50 L10 28 Q10 15 23 15 L41 15 Q54 15 54 28 L54 50 Z
           M15.2 50 L15.2 28 Q15.2 20.2 23 20.2 L41 20.2 Q48.8 20.2 48.8 28 L48.8 50 Z"
        fillRule="evenodd"
        fill={`url(#${id}-metal)`}
      />

      {/* Piso do box: passa por baixo do portico e vaza para os lados. */}
      <rect x="6" y="50" width="52" height="2.8" rx="1.4" fill={`url(#${id}-piso)`} />
    </svg>
  )
}

/**
 * Assinatura horizontal: simbolo + nome tipografico. E o que vai na barra
 * lateral e no cabecalho — o letreiro empilhado e alto demais para uma barra.
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
          Track<span className="text-tinta-500">Bay</span>
        </span>
      )}
    </span>
  )
}
