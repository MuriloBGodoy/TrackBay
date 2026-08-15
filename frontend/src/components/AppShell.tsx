import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  CalendarBlank,
  CaretLeft,
  CarProfile,
  GearSix,
  HouseSimple,
  MagnifyingGlass,
  Package,
  Plus,
  SignOut,
  UsersThree,
  Wrench,
  type Icon,
} from '@phosphor-icons/react'
import { useAuth } from '../auth/AuthContext'
import { MarcaSimbolo } from './Marca'
import { Botao, cx } from './ui'
import { ROTULO_PAPEL } from '../lib/rotulos'

interface ItemNav {
  para: string
  rotulo: string
  Icone: Icon
  fim?: boolean
}

/** Navegacao completa — a barra lateral mostra tudo. */
const NAV: ItemNav[] = [
  { para: '/app', rotulo: 'Início', Icone: HouseSimple, fim: true },
  { para: '/app/ordens', rotulo: 'Ordens de serviço', Icone: Wrench },
  { para: '/app/clientes', rotulo: 'Clientes', Icone: UsersThree },
  { para: '/app/veiculos', rotulo: 'Veículos', Icone: CarProfile },
  { para: '/app/estoque', rotulo: 'Estoque', Icone: Package },
  { para: '/app/agenda', rotulo: 'Agenda', Icone: CalendarBlank },
]

/** No celular o polegar alcanca cinco alvos; o resto fica na tela de config. */
const NAV_CURTA = NAV.filter((i) => i.para !== '/app/veiculos')

const CHAVE_COLAPSO = 'tw:sidebar-colapsada'

/* O prefixo "use" e exigencia do lint de hooks — o resto do codigo segue em pt-BR. */
function useColapso() {
  const [colapsada, setColapsada] = useState(
    () => globalThis.localStorage?.getItem(CHAVE_COLAPSO) === '1',
  )
  useEffect(() => {
    globalThis.localStorage?.setItem(CHAVE_COLAPSO, colapsada ? '1' : '0')
  }, [colapsada])
  return [colapsada, setColapsada] as const
}

/** Busca por placa: a acao mais frequente do balcao, presente em toda tela. */
function BuscaPlaca({ className }: { className?: string }) {
  const navigate = useNavigate()
  const [busca, setBusca] = useState('')

  const enviar = (e: React.FormEvent) => {
    e.preventDefault()
    const termo = busca.trim()
    if (!termo) return
    navigate(`/app/veiculos?busca=${encodeURIComponent(termo)}`)
    setBusca('')
  }

  return (
    <form onSubmit={enviar} className={cx('relative', className)}>
      <MagnifyingGlass
        size={17}
        className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-tinta-500"
      />
      <input
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        placeholder="Buscar placa, cliente…"
        aria-label="Buscar por placa ou cliente"
        className="min-h-11 w-full rounded-full border border-white/10 bg-white/[0.04] pr-4 pl-10 text-base transition-colors placeholder:text-tinta-500 hover:border-white/20 focus:outline-2 focus:outline-white/70"
      />
    </form>
  )
}

function LinkNav({
  item,
  colapsada,
  aoNavegar,
}: {
  item: ItemNav
  colapsada: boolean
  aoNavegar?: () => void
}) {
  const { para, rotulo, Icone, fim } = item
  return (
    <NavLink
      to={para}
      end={fim}
      onClick={aoNavegar}
      title={colapsada ? rotulo : undefined}
      className={({ isActive }) =>
        cx(
          'group relative flex h-11 items-center rounded-xl text-sm font-semibold transition-colors',
          colapsada ? 'mx-auto w-11 justify-center' : 'gap-3 px-3',
          isActive
            ? 'bg-white text-black shadow-[var(--tk-glow)]'
            : 'text-tinta-600 hover:bg-white/[0.07] hover:text-tinta-900',
        )
      }
    >
      {({ isActive }) => (
        <>
          <Icone size={20} weight={isActive ? 'fill' : 'regular'} className="shrink-0" />
          {!colapsada && <span className="truncate">{rotulo}</span>}
        </>
      )}
    </NavLink>
  )
}

function BarraLateral({
  colapsada,
  aoAlternar,
}: {
  colapsada: boolean
  aoAlternar: () => void
}) {
  const { sessao, sairDaConta } = useAuth()
  const usuario = sessao?.usuario

  return (
    <aside
      className={cx(
        'sticky top-0 hidden h-dvh shrink-0 flex-col border-r border-white/8 bg-[var(--tk-surface)]/70 backdrop-blur-xl transition-[width] duration-300 ease-out lg:flex',
        colapsada ? 'w-[76px]' : 'w-[252px]',
      )}
    >
      {/* Marca */}
      <div
        className={cx(
          'flex h-[68px] shrink-0 items-center border-b border-white/8',
          colapsada ? 'justify-center px-3' : 'px-4',
        )}
      >
        <NavLink to="/app" end className="flex items-center gap-2.5 overflow-hidden">
          <MarcaSimbolo tamanho={34} />
          {!colapsada && (
            <span className="font-display text-[15px] leading-none font-bold tracking-tight whitespace-nowrap text-tinta-900">
              Track<span className="text-tinta-500">Bay</span>
            </span>
          )}
        </NavLink>
      </div>

      <nav className="tk-scroll min-h-0 flex-1 space-y-1 overflow-y-auto px-3 py-4">
        {NAV.map((item) => (
          <LinkNav key={item.para} item={item} colapsada={colapsada} />
        ))}

        <div className="!my-3 border-t border-white/8" />

        <LinkNav
          item={{ para: '/app/config', rotulo: 'Configurações', Icone: GearSix }}
          colapsada={colapsada}
        />
      </nav>

      {/* Recolher / expandir */}
      <div className="shrink-0 px-3 pb-2">
        <button
          type="button"
          onClick={aoAlternar}
          aria-label={colapsada ? 'Expandir menu' : 'Recolher menu'}
          title={colapsada ? 'Expandir menu' : 'Recolher menu'}
          className="flex h-10 w-full items-center justify-center rounded-xl text-tinta-500 transition-colors hover:bg-white/[0.07] hover:text-tinta-900"
        >
          <CaretLeft
            size={18}
            weight="bold"
            className={cx('transition-transform duration-300', colapsada && 'rotate-180')}
          />
        </button>
      </div>

      {/* Quem esta usando o app */}
      <div className="shrink-0 border-t border-white/8 px-3 pt-3 pb-[max(env(safe-area-inset-bottom),1rem)]">
        <div className={cx('flex items-center rounded-xl p-1.5', colapsada ? 'justify-center' : 'gap-2.5')}>
          {usuario?.fotoUrl ? (
            <img
              src={usuario.fotoUrl}
              alt=""
              className="size-9 shrink-0 rounded-full object-cover ring-1 ring-white/20 grayscale"
            />
          ) : (
            <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-white/[0.08] font-display text-xs font-bold text-tinta-800 ring-1 ring-white/12">
              {(usuario?.nome ?? '?').slice(0, 2).toUpperCase()}
            </span>
          )}
          {!colapsada && (
            <>
              <div className="min-w-0 flex-1">
                <p className="truncate text-[13px] font-semibold text-tinta-900">
                  {usuario?.nome ?? 'Usuário'}
                </p>
                <p className="truncate text-[11px] text-tinta-500">
                  {usuario ? ROTULO_PAPEL[usuario.papel] : ''}
                </p>
              </div>
              <button
                type="button"
                onClick={() => void sairDaConta()}
                title="Sair"
                aria-label="Sair"
                className="flex size-8 shrink-0 items-center justify-center rounded-lg text-tinta-500 transition-colors hover:bg-white/[0.07] hover:text-tinta-900"
              >
                <SignOut size={17} />
              </button>
            </>
          )}
        </div>
      </div>
    </aside>
  )
}

function BarraTopo() {
  const { sessao, sairDaConta } = useAuth()

  return (
    <header className="tk-glass sticky top-0 z-20 border-x-0 border-t-0">
      <div className="flex items-center gap-3 p-3 sm:px-5">
        {/* Abaixo de lg nao ha barra lateral: a marca volta para o topo. */}
        <NavLink to="/app" end className="flex shrink-0 items-center gap-2.5 lg:hidden">
          <MarcaSimbolo tamanho={32} />
          <span className="hidden font-display text-[15px] leading-none font-bold tracking-tight text-tinta-900 sm:inline">
            Track<span className="text-tinta-500">Bay</span>
          </span>
        </NavLink>

        <BuscaPlaca className="min-w-0 flex-1" />

        <NavLink to="/app/ordens/nova" className="hidden sm:block">
          <Botao className="whitespace-nowrap">
            <Plus size={16} weight="bold" />
            Nova OS
          </Botao>
        </NavLink>

        <NavLink
          to="/app/config"
          title="Configurações"
          aria-label="Configurações"
          className="flex size-11 shrink-0 items-center justify-center rounded-full text-tinta-600 transition-colors hover:bg-white/[0.07] hover:text-tinta-900 lg:hidden"
        >
          <GearSix size={20} />
        </NavLink>
        <button
          type="button"
          onClick={() => void sairDaConta()}
          title={sessao?.usuario.nome ?? 'Sair'}
          aria-label="Sair"
          className="flex size-11 shrink-0 items-center justify-center rounded-full text-tinta-600 transition-colors hover:bg-white/[0.07] hover:text-tinta-900 lg:hidden"
        >
          {sessao?.usuario.fotoUrl ? (
            <img
              src={sessao.usuario.fotoUrl}
              alt=""
              className="size-8 rounded-full object-cover ring-1 ring-white/25 grayscale"
            />
          ) : (
            <SignOut size={20} />
          )}
        </button>
      </div>

      {/* Faixa de navegacao entre o celular e o desktop, onde ainda nao ha barra lateral. */}
      <nav className="-mt-1 hidden gap-1 overflow-x-auto px-3 pb-2 sem-barra sm:flex sm:px-5 lg:hidden">
        {NAV.map(({ para, rotulo, Icone, fim }) => (
          <NavLink
            key={para}
            to={para}
            end={fim}
            className={({ isActive }) =>
              cx(
                'flex shrink-0 items-center gap-2 rounded-full px-3.5 py-2 text-sm font-semibold transition-colors',
                isActive
                  ? 'bg-white text-black'
                  : 'text-tinta-600 hover:bg-white/[0.07] hover:text-tinta-900',
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icone size={16} weight={isActive ? 'fill' : 'regular'} />
                {rotulo}
              </>
            )}
          </NavLink>
        ))}
      </nav>
    </header>
  )
}

function BarraInferior() {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-20 px-4 pt-1 pb-[max(0.7rem,env(safe-area-inset-bottom))] sm:hidden">
      <div className="tk-glass mx-auto flex max-w-sm items-center rounded-full p-1.5 shadow-[var(--tk-shadow-lg)]">
        {NAV_CURTA.map(({ para, rotulo, Icone, fim }) => (
          <NavLink
            key={para}
            to={para}
            end={fim}
            aria-label={rotulo}
            className={({ isActive }) =>
              cx(
                'flex min-w-0 flex-1 items-center justify-center rounded-full py-2.5 transition-all',
                isActive ? 'bg-white text-black shadow-[var(--tk-glow)]' : 'text-tinta-500',
              )
            }
          >
            {({ isActive }) => <Icone size={21} weight={isActive ? 'fill' : 'regular'} />}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}

/**
 * Casca do app logado: barra lateral no desktop, faixa de abas no tablet e
 * barra flutuante no celular. O conteudo da rota entra no <Outlet />.
 */
export function AppShell() {
  const [colapsada, setColapsada] = useColapso()

  return (
    <div className="flex min-h-full">
      <BarraLateral colapsada={colapsada} aoAlternar={() => setColapsada((v) => !v)} />

      <div className="flex min-w-0 flex-1 flex-col">
        <BarraTopo />
        <main className="mx-auto w-full max-w-[1360px] flex-1 p-3 pb-28 sm:p-5 sm:pb-8 lg:p-7">
          <Outlet />
        </main>
      </div>

      <BarraInferior />
    </div>
  )
}
