import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight } from '@phosphor-icons/react'
import { useAuth } from '../auth/AuthContext'
import { MarcaSimbolo } from './Marca'
import { Botao, cx } from './ui'

/**
 * ===== Peças da vitrine pública =====
 *
 * O que a home e a pagina "Como funciona" dividem: barra, rodape, moldura de
 * secao e a chamada final. Fica aqui, e nao dentro de uma das paginas, para as
 * duas nao sairem do lugar quando so uma for mexida.
 *
 * A vitrine e publica de proposito, mas o produto e por assinatura: a home nao
 * precisa convencer estranho, precisa deixar quem ja assinou entrar rapido.
 * Por isso o botao de entrar aparece na barra, no heroi e no rodape.
 */

/** Para onde o botao de entrar leva, e como ele se chama. */
export function useEntrada() {
  const { sessao, modoDev, usuarioFirebase } = useAuth()

  // Quem ja esta logado nao precisa passar pela tela de login de novo.
  const logado = modoDev || !!usuarioFirebase
  const pronto = logado && !sessao?.precisaOnboarding

  return {
    destino: pronto ? '/app' : '/login',
    rotuloEntrar: pronto ? 'Abrir painel' : 'Entrar',
  }
}

/**
 * Revela o bloco quando ele entra na tela. Sem isto a pagina inteira aparece
 * de uma vez e a rolagem fica sem ritmo.
 */
export function useRevelar<T extends HTMLElement>() {
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

export function Secao({
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
        {descricao && <p className="mt-4 text-base leading-relaxed text-tinta-600">{descricao}</p>}
        {acao && <div className="mt-6">{acao}</div>}
      </header>
      {children}
    </section>
  )
}

/** Marca + nome, do jeito que aparece na barra e no rodape. */
function Assinatura({ tamanho }: { tamanho: number }) {
  return (
    <Link to="/" className="flex shrink-0 items-center gap-2.5">
      <MarcaSimbolo tamanho={tamanho} />
      <span className="font-display text-[15px] leading-none font-bold tracking-tight text-tinta-900">
        Track<span className="text-tinta-500">Bay</span>
      </span>
    </Link>
  )
}

export function BarraPublica() {
  const { destino, rotuloEntrar } = useEntrada()
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
        <Assinatura tamanho={36} />

        <Link
          to="/como-funciona"
          className="ml-auto rounded-full px-3.5 py-2 text-sm font-semibold text-tinta-600 transition-colors hover:bg-white/[0.07] hover:text-tinta-900"
        >
          Como funciona
        </Link>

        <Link to={destino} className="shrink-0">
          <Botao>
            {rotuloEntrar}
            <ArrowRight size={16} weight="bold" />
          </Botao>
        </Link>
      </div>
    </header>
  )
}

export function ChamadaFinal() {
  const { destino, rotuloEntrar } = useEntrada()
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

export function Rodape() {
  return (
    <footer className="border-t border-white/8">
      <div className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-10 sm:flex-row sm:items-start sm:justify-between sm:px-6">
        <div>
          <Assinatura tamanho={34} />
          <p className="mt-3 max-w-xs text-sm leading-relaxed text-tinta-500">
            Gestão para oficinas mecânicas, com a Ordem de Serviço que fala a língua do seu ramo.
          </p>
        </div>

        <nav className="flex flex-wrap gap-x-8 gap-y-3">
          <Link
            to="/como-funciona"
            className="text-sm font-semibold text-tinta-600 transition-colors hover:text-tinta-900"
          >
            Como funciona
          </Link>
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
          © {new Date().getFullYear()} Track Bay · Dados tratados conforme a LGPD
        </p>
      </div>
    </footer>
  )
}
