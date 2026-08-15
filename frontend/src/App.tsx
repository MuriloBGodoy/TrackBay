import { lazy, Suspense, useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { Carregando } from './components/ui'
import { PaginaHome } from './pages/PaginaHome'
import { PaginaLogin } from './pages/PaginaLogin'

/*
 * A vitrine publica e o login vem no pacote inicial — sao a porta de entrada.
 * O app logado e tudo o que ele arrasta (motor de campos, graficos, telas de
 * cadastro) so baixa depois que alguem realmente entra. "Como funciona" tambem
 * e publica, mas fica fora do pacote inicial: quem so quer entrar nao paga por
 * ela.
 */
const PaginaComoFunciona = lazy(() =>
  import('./pages/PaginaComoFunciona').then((m) => ({ default: m.PaginaComoFunciona })),
)
const AppShell = lazy(() => import('./components/AppShell').then((m) => ({ default: m.AppShell })))
const PaginaOnboarding = lazy(() =>
  import('./pages/PaginaOnboarding').then((m) => ({ default: m.PaginaOnboarding })),
)
const PaginaInicio = lazy(() =>
  import('./pages/PaginaInicio').then((m) => ({ default: m.PaginaInicio })),
)
const PaginaOrdens = lazy(() =>
  import('./pages/PaginaOrdens').then((m) => ({ default: m.PaginaOrdens })),
)
const PaginaOrdemDetalhe = lazy(() =>
  import('./pages/PaginaOrdemDetalhe').then((m) => ({ default: m.PaginaOrdemDetalhe })),
)
const PaginaNovaOrdem = lazy(() =>
  import('./pages/PaginaNovaOrdem').then((m) => ({ default: m.PaginaNovaOrdem })),
)
const PaginaClientes = lazy(() =>
  import('./pages/PaginaClientes').then((m) => ({ default: m.PaginaClientes })),
)
const PaginaVeiculos = lazy(() =>
  import('./pages/PaginaVeiculos').then((m) => ({ default: m.PaginaVeiculos })),
)
const PaginaEstoque = lazy(() =>
  import('./pages/PaginaEstoque').then((m) => ({ default: m.PaginaEstoque })),
)
const PaginaAgenda = lazy(() =>
  import('./pages/PaginaAgenda').then((m) => ({ default: m.PaginaAgenda })),
)
const PaginaConfig = lazy(() =>
  import('./pages/PaginaConfig').then((m) => ({ default: m.PaginaConfig })),
)

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Oficina tem sinal ruim no box: nao refazer request a cada foco de janela.
      refetchOnWindowFocus: false,
      staleTime: 30_000,
      retry: 1,
    },
  },
})

/**
 * Troca de rota comeca no topo. Sem isto, sair do rodape da home para "Como
 * funciona" abriria a pagina nova ja rolada ate o fim. Ancora (#secao) e a
 * excecao: ali o alvo e o proprio destino.
 */
function RolarAoTopo() {
  const { pathname, hash } = useLocation()

  useEffect(() => {
    if (hash) return
    window.scrollTo(0, 0)
  }, [pathname, hash])

  return null
}

/** Guarda de rota: sem login vai para o login; sem oficina, para o onboarding. */
function Protegido() {
  const { sessao, carregando, modoDev, usuarioFirebase } = useAuth()

  if (carregando) {
    return <Carregando texto="Entrando…" />
  }
  if (!modoDev && !usuarioFirebase) {
    return <Navigate to="/login" replace />
  }
  if (sessao?.precisaOnboarding) {
    return <Navigate to="/onboarding" replace />
  }
  return <AppShell />
}

/**
 * "/" e a vitrine publica; o produto inteiro mora sob "/app". Separar os dois
 * deixa a landing carregar sem sessao e o app manter uma casca so dele.
 */
function Rotas() {
  return (
    <Routes>
      <Route path="/" element={<PaginaHome />} />
      <Route path="/como-funciona" element={<PaginaComoFunciona />} />
      <Route path="/login" element={<PaginaLogin />} />
      <Route path="/onboarding" element={<PaginaOnboarding />} />

      <Route path="/app" element={<Protegido />}>
        <Route index element={<PaginaInicio />} />
        <Route path="ordens" element={<PaginaOrdens />} />
        <Route path="ordens/nova" element={<PaginaNovaOrdem />} />
        <Route path="ordens/:id" element={<PaginaOrdemDetalhe />} />
        <Route path="clientes" element={<PaginaClientes />} />
        <Route path="veiculos" element={<PaginaVeiculos />} />
        <Route path="estoque" element={<PaginaEstoque />} />
        <Route path="agenda" element={<PaginaAgenda />} />
        <Route path="config" element={<PaginaConfig />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <RolarAoTopo />
          <Suspense fallback={<Carregando />}>
            <Rotas />
          </Suspense>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
