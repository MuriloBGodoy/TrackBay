import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clientesApi } from '../api/recursos'
import { Plus } from '@phosphor-icons/react'
import {
  Botao,
  CabecalhoPagina,
  Campo,
  Cartao,
  Carregando,
  ErroBox,
  Input,
  Vazio,
  cx,
} from '../components/ui'
import {
  documentoFormatado,
  mascararCnpj,
  mascararCpf,
  mascararTelefone,
  telefoneFormatado,
} from '../lib/formato'
import { ROTULO_TIPO_PESSOA } from '../lib/rotulos'
import type { Cliente, TipoPessoa } from '../types/dominio'

export function PaginaClientes() {
  const [busca, setBusca] = useState('')
  const [criando, setCriando] = useState(false)

  const { data: clientes, isLoading } = useQuery({
    queryKey: ['clientes', busca],
    queryFn: () => clientesApi.listar(busca || undefined),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Clientes"
        acoes={
          <Botao onClick={() => setCriando(true)}>
            <Plus size={16} weight="bold" />
            Novo
          </Botao>
        }
      />

      <Input
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
        placeholder="Buscar por nome, documento ou telefone"
      />

      {criando && <FormularioCliente aoFechar={() => setCriando(false)} />}

      {isLoading ? (
        <Carregando />
      ) : !clientes?.length ? (
        <Cartao>
          <Vazio titulo="Nenhum cliente" descricao="Cadastre o primeiro cliente da sua oficina." />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {clientes.map((c) => (
            <li key={c.id}>
              <Cartao>
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate font-semibold">{c.nome}</p>
                    <p className="text-sm text-tinta-500">
                      {documentoFormatado(c.dadosPF?.cpf ?? c.dadosPJ?.cnpj)}
                    </p>
                    {c.telefone && (
                      <a
                        href={`tel:${c.telefone}`}
                        className="text-sm text-tinta-600 underline-offset-2 hover:text-tinta-900 hover:underline"
                      >
                        {telefoneFormatado(c.telefone)}
                      </a>
                    )}
                  </div>
                  <span
                    className={cx(
                      'shrink-0 rounded-full px-2 py-0.5 text-xs font-bold',
                      c.tipoPessoa === 'JURIDICA'
                        ? 'bg-white text-black'
                        : 'border border-white/15 text-tinta-600',
                    )}
                  >
                    {c.tipoPessoa === 'JURIDICA' ? 'PJ' : 'PF'}
                  </span>
                </div>
              </Cartao>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

/** O formulario muda conforme PF ou PJ — a validacao no backend segue a mesma regra. */
function FormularioCliente({ aoFechar }: { aoFechar: () => void }) {
  const queryClient = useQueryClient()
  const [tipoPessoa, setTipoPessoa] = useState<TipoPessoa>('FISICA')
  const [nome, setNome] = useState('')
  const [documento, setDocumento] = useState('')
  const [telefone, setTelefone] = useState('')
  const [email, setEmail] = useState('')
  const [consentimento, setConsentimento] = useState(true)
  const [erro, setErro] = useState<string>()

  const criar = useMutation({
    mutationFn: clientesApi.criar,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['clientes'] })
      aoFechar()
    },
    onError: (e: Error) => setErro(e.message),
  })

  const enviar = (e: React.FormEvent) => {
    e.preventDefault()
    setErro(undefined)
    const doc = documento.replace(/\D/g, '')
    const cliente: Partial<Cliente> = {
      tipoPessoa,
      nome,
      telefone: telefone.replace(/\D/g, '') || undefined,
      email: email || undefined,
      consentimentoLgpd: consentimento,
      ...(tipoPessoa === 'FISICA'
        ? { dadosPF: { cpf: doc || undefined } }
        : { dadosPJ: { cnpj: doc, razaoSocial: nome } }),
    }
    criar.mutate(cliente)
  }

  return (
    <Cartao>
      <form onSubmit={enviar} className="space-y-3">
        <div className="flex gap-2">
          {(['FISICA', 'JURIDICA'] as TipoPessoa[]).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => {
                setTipoPessoa(t)
                setDocumento('')
              }}
              className={cx(
                'min-h-11 flex-1 rounded-xl border text-sm font-semibold',
                tipoPessoa === t
                  ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                  : 'border-white/10 bg-white/[0.04] text-tinta-600 hover:border-white/25 hover:text-tinta-900',
              )}
            >
              {ROTULO_TIPO_PESSOA[t]}
            </button>
          ))}
        </div>

        {erro && <ErroBox mensagem={erro} />}

        <Campo rotulo={tipoPessoa === 'FISICA' ? 'Nome' : 'Nome fantasia'} obrigatorio>
          <Input value={nome} onChange={(e) => setNome(e.target.value)} autoFocus />
        </Campo>

        <Campo
          rotulo={tipoPessoa === 'FISICA' ? 'CPF' : 'CNPJ'}
          obrigatorio={tipoPessoa === 'JURIDICA'}
        >
          <Input
            value={documento}
            inputMode="numeric"
            onChange={(e) =>
              setDocumento(
                tipoPessoa === 'FISICA' ? mascararCpf(e.target.value) : mascararCnpj(e.target.value),
              )
            }
            placeholder={tipoPessoa === 'FISICA' ? '000.000.000-00' : '00.000.000/0000-00'}
          />
        </Campo>

        <Campo rotulo="Telefone">
          <Input
            value={telefone}
            inputMode="tel"
            onChange={(e) => setTelefone(mascararTelefone(e.target.value))}
            placeholder="(11) 98765-4321"
          />
        </Campo>

        <Campo rotulo="E-mail">
          <Input
            type="email"
            inputMode="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Campo>

        <label className="flex items-start gap-2 text-sm text-tinta-600">
          <input
            type="checkbox"
            checked={consentimento}
            onChange={(e) => setConsentimento(e.target.checked)}
            className="mt-0.5 size-4"
          />
          <span>Cliente autorizou o uso dos dados pessoais (LGPD)</span>
        </label>

        <div className="flex gap-2 pt-1">
          <Botao type="button" variante="secundario" onClick={aoFechar} className="flex-1">
            Cancelar
          </Botao>
          <Botao type="submit" disabled={criar.isPending} className="flex-1">
            {criar.isPending ? 'Salvando…' : 'Salvar'}
          </Botao>
        </div>
      </form>
    </Cartao>
  )
}
