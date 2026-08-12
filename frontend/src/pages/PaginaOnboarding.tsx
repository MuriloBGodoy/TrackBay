import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { MarcaSimbolo } from '../components/Marca'
import { sessaoApi, templatesApi } from '../api/recursos'
import { useAuth } from '../auth/AuthContext'
import { Botao, Campo, Cartao, ErroBox, Input, cx } from '../components/ui'
import { mascararCnpj, mascararTelefone } from '../lib/formato'
import type { Ramo } from '../types/dominio'

/** Onboarding: escolher o ramo aqui e o que faz a OS nascer com os campos certos. */
export function PaginaOnboarding() {
  const navigate = useNavigate()
  const { recarregarSessao } = useAuth()
  const [nomeFantasia, setNomeFantasia] = useState('')
  const [cnpj, setCnpj] = useState('')
  const [telefone, setTelefone] = useState('')
  const [whatsapp, setWhatsapp] = useState('')
  const [ramos, setRamos] = useState<Ramo[]>([])
  const [erro, setErro] = useState<string>()

  const { data: ramosDisponiveis } = useQuery({ queryKey: ['ramos'], queryFn: templatesApi.ramos })

  const criar = useMutation({
    mutationFn: sessaoApi.onboarding,
    onSuccess: () => {
      recarregarSessao()
      navigate('/app', { replace: true })
    },
    onError: (e: Error) => setErro(e.message),
  })

  const alternarRamo = (ramo: Ramo) => {
    setRamos((atual) => (atual.includes(ramo) ? atual.filter((r) => r !== ramo) : [...atual, ramo]))
  }

  const enviar = (e: React.FormEvent) => {
    e.preventDefault()
    setErro(undefined)
    if (!nomeFantasia.trim()) return setErro('Informe o nome da oficina')
    if (!ramos.length) return setErro('Escolha ao menos um ramo de atuação')

    criar.mutate({
      nomeFantasia,
      cnpj: cnpj.replace(/\D/g, '') || undefined,
      telefone: telefone.replace(/\D/g, '') || undefined,
      whatsapp: whatsapp.replace(/\D/g, '') || undefined,
      ramos,
    })
  }

  return (
    <div className="mx-auto max-w-lg p-4">
      <header className="mb-6 text-center">
        <span className="inline-flex">
          <MarcaSimbolo tamanho={52} />
        </span>
        <h1 className="mt-3 text-2xl font-extrabold tracking-tight text-tinta-900">
          Vamos configurar sua oficina
        </h1>
        <p className="mt-1.5 text-sm text-tinta-600">
          O ramo define os campos que aparecem na Ordem de Serviço. Você pode ajustar depois.
        </p>
      </header>

      <form onSubmit={enviar} className="space-y-4">
        {erro && <ErroBox mensagem={erro} />}

        <Cartao className="space-y-4">
          <Campo rotulo="Nome da oficina" obrigatorio>
            <Input
              value={nomeFantasia}
              onChange={(e) => setNomeFantasia(e.target.value)}
              placeholder="Ex.: Radiadores do Zé"
              autoFocus
            />
          </Campo>

          <Campo rotulo="CNPJ" dica="Opcional — usado no PDF da OS">
            <Input
              value={cnpj}
              onChange={(e) => setCnpj(mascararCnpj(e.target.value))}
              inputMode="numeric"
              placeholder="00.000.000/0000-00"
            />
          </Campo>

          <div className="grid grid-cols-2 gap-3">
            <Campo rotulo="Telefone">
              <Input
                value={telefone}
                onChange={(e) => setTelefone(mascararTelefone(e.target.value))}
                inputMode="tel"
                placeholder="(11) 3456-7890"
              />
            </Campo>
            <Campo rotulo="WhatsApp">
              <Input
                value={whatsapp}
                onChange={(e) => setWhatsapp(mascararTelefone(e.target.value))}
                inputMode="tel"
                placeholder="(11) 98765-4321"
              />
            </Campo>
          </div>
        </Cartao>

        <Cartao>
          <p className="mb-3 text-sm font-medium text-tinta-700">
            Ramo de atuação <span className="text-red-400">*</span>
          </p>
          <div className="flex flex-wrap gap-2">
            {ramosDisponiveis?.map((ramo) => {
              const ativo = ramos.includes(ramo.valor)
              return (
                <button
                  key={ramo.valor}
                  type="button"
                  aria-pressed={ativo}
                  onClick={() => alternarRamo(ramo.valor)}
                  className={cx(
                    'min-h-11 rounded-xl border px-3.5 text-sm font-semibold transition-all',
                    ativo
                      ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                      : 'border-white/10 bg-white/[0.04] text-tinta-600 hover:border-white/25 hover:text-tinta-900',
                  )}
                >
                  {ramo.rotulo}
                </button>
              )
            })}
          </div>
          <p className="mt-3 text-xs text-tinta-500">
            Pode escolher mais de um. Cada ramo traz seu catálogo de campos pronto.
          </p>
        </Cartao>

        <Botao type="submit" tamanho="grande" className="w-full" disabled={criar.isPending}>
          {criar.isPending ? 'Criando…' : 'Criar oficina'}
        </Botao>
      </form>
    </div>
  )
}
