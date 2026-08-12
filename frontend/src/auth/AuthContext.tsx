import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import type { User } from 'firebase/auth'
import { MODO_DEV, entrarComGoogle, observarAuth, sair } from './firebase'
import { sessaoApi } from '../api/recursos'
import type { Sessao } from '../types/dominio'

interface AuthContexto {
  usuarioFirebase: User | null
  sessao: Sessao | undefined
  carregando: boolean
  modoDev: boolean
  entrar: () => Promise<void>
  sairDaConta: () => Promise<void>
  recarregarSessao: () => void
}

const Contexto = createContext<AuthContexto | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuarioFirebase, setUsuarioFirebase] = useState<User | null>(null)
  // No modo dev nao ha Firebase: ja entramos prontos para chamar a API.
  const [authResolvida, setAuthResolvida] = useState(MODO_DEV)
  const queryClient = useQueryClient()

  useEffect(() => {
    return observarAuth((usuario) => {
      setUsuarioFirebase(usuario)
      setAuthResolvida(true)
    })
  }, [])

  const logado = MODO_DEV || !!usuarioFirebase

  const { data: sessao, isLoading } = useQuery({
    queryKey: ['sessao'],
    queryFn: sessaoApi.me,
    enabled: authResolvida && logado,
    retry: false,
  })

  const valor = useMemo<AuthContexto>(
    () => ({
      usuarioFirebase,
      sessao,
      carregando: !authResolvida || (logado && isLoading),
      modoDev: MODO_DEV,
      entrar: async () => {
        await entrarComGoogle()
        await queryClient.invalidateQueries({ queryKey: ['sessao'] })
      },
      sairDaConta: async () => {
        await sair()
        queryClient.clear()
      },
      recarregarSessao: () => {
        void queryClient.invalidateQueries({ queryKey: ['sessao'] })
      },
    }),
    [usuarioFirebase, sessao, authResolvida, logado, isLoading, queryClient],
  )

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

export function useAuth(): AuthContexto {
  const ctx = useContext(Contexto)
  if (!ctx) {
    throw new Error('useAuth precisa estar dentro de AuthProvider')
  }
  return ctx
}
