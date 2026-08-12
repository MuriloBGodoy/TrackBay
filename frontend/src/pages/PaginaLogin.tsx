import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { ArrowLeft, CheckCircle, GoogleLogo } from '@phosphor-icons/react'
import { useAuth } from '../auth/AuthContext'
import { Botao, ErroBox } from '../components/ui'
import { MarcaSimbolo, MarcaTexto } from '../components/Marca'

const DESTAQUES = [
  'Ordem de serviço no celular, do orçamento à entrega',
  'Pátio da oficina: veja cada carro na sua baia',
  'PDF pronto para mandar no WhatsApp do cliente',
]

export function PaginaLogin() {
  const { entrar, usuarioFirebase, modoDev } = useAuth()
  const [erro, setErro] = useState<string>()
  const [entrando, setEntrando] = useState(false)

  if (modoDev || usuarioFirebase) {
    return <Navigate to="/app" replace />
  }

  const fazerLogin = async () => {
    setEntrando(true)
    setErro(undefined)
    try {
      await entrar()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível entrar. Tente novamente.')
    } finally {
      setEntrando(false)
    }
  }

  return (
    <div className="flex min-h-full items-center justify-center p-4">
      <div className="w-full max-w-4xl">
        <Link
          to="/"
          className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-tinta-500 transition-colors hover:text-tinta-900"
        >
          <ArrowLeft size={15} weight="bold" />
          Voltar para o site
        </Link>

        {/* Split no desktop: no celular o painel-heroi sai e sobra o essencial. */}
        <div className="tk-card grid overflow-hidden !rounded-3xl shadow-[var(--tk-shadow-lg)] lg:grid-cols-[1.05fr_1fr]">
          <aside className="tk-hero hidden flex-col justify-between gap-10 !rounded-none border-0 p-10 lg:flex">
            <span className="w-fit rounded-full border border-white/12 bg-white/[0.05] px-3 py-1.5 text-[11px] font-bold tracking-[0.2em] text-tinta-600 uppercase">
              Gestão para oficinas
            </span>

            <div>
              <MarcaTexto className="mb-6 w-52 max-w-full" />
              <p className="max-w-sm text-sm leading-relaxed text-tinta-600">
                O carro entra, a OS abre e ele ganha uma baia no pátio. Tudo o que a oficina faz no
                dia, registrado sem papel.
              </p>
            </div>

            <ul className="space-y-3">
              {DESTAQUES.map((texto) => (
                <li key={texto} className="flex items-start gap-2.5 text-sm text-tinta-700">
                  <CheckCircle size={17} weight="fill" className="mt-0.5 shrink-0 text-tinta-900" />
                  {texto}
                </li>
              ))}
            </ul>
          </aside>

          <div className="flex flex-col justify-center gap-6 p-8 sm:p-10">
            {/* O painel-heroi some abaixo de lg; sem isto o celular ficaria sem marca. */}
            <div className="flex flex-col items-center gap-2.5 lg:hidden">
              <MarcaSimbolo tamanho={56} />
              <MarcaTexto className="w-28" />
            </div>

            <div className="text-center lg:text-left">
              <h1 className="font-display text-2xl font-bold tracking-tight text-tinta-900">
                Entrar na sua oficina
              </h1>
              <p className="mt-1.5 text-sm text-tinta-600">
                Use a conta Google da oficina para acessar.
              </p>
            </div>

            {erro && <ErroBox mensagem={erro} />}

            <Botao onClick={fazerLogin} disabled={entrando} tamanho="grande" className="w-full">
              <GoogleLogo size={19} weight="bold" />
              {entrando ? 'Entrando…' : 'Entrar com Google'}
            </Botao>

            <p className="text-center text-xs leading-relaxed text-tinta-500 lg:text-left">
              Ao entrar você concorda com o tratamento dos seus dados conforme a LGPD.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
