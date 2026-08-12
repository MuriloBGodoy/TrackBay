import { useEffect, useState } from 'react'
import { DeviceMobile } from '@phosphor-icons/react'
import { Botao } from './ui'

/** O evento do Chrome/Edge que permite disparar a instalacao pelo nosso botao. */
interface EventoInstalacao extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

const CHAVE_RECUSA = 'tw:instalacao-recusada'

/**
 * Botao "instalar app". So aparece quando o navegador diz que da para instalar
 * — ou seja, nunca no iOS (que exige o caminho manual pelo Compartilhar) e nem
 * quando o app ja esta instalado. Some de vez se a pessoa recusar.
 */
export function PromptInstalar({
  tamanho = 'medio',
  className,
}: {
  tamanho?: 'medio' | 'grande'
  className?: string
}) {
  const [evento, setEvento] = useState<EventoInstalacao | null>(null)

  useEffect(() => {
    if (globalThis.localStorage?.getItem(CHAVE_RECUSA) === '1') return

    const aoPoderInstalar = (e: Event) => {
      // Sem o preventDefault o Chrome mostra a barra dele por cima da nossa UI.
      e.preventDefault()
      setEvento(e as EventoInstalacao)
    }
    const aoInstalar = () => setEvento(null)

    window.addEventListener('beforeinstallprompt', aoPoderInstalar)
    window.addEventListener('appinstalled', aoInstalar)
    return () => {
      window.removeEventListener('beforeinstallprompt', aoPoderInstalar)
      window.removeEventListener('appinstalled', aoInstalar)
    }
  }, [])

  if (!evento) return null

  const instalar = async () => {
    await evento.prompt()
    const { outcome } = await evento.userChoice
    if (outcome === 'dismissed') {
      globalThis.localStorage?.setItem(CHAVE_RECUSA, '1')
    }
    setEvento(null)
  }

  return (
    <Botao
      variante="secundario"
      tamanho={tamanho}
      className={className}
      onClick={() => void instalar()}
    >
      <DeviceMobile size={17} weight="duotone" />
      Instalar o app
    </Botao>
  )
}
