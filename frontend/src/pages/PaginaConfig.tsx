import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { sessaoApi, templatesApi } from '../api/recursos'
import { useAuth } from '../auth/AuthContext'
import { CabecalhoPagina, Cartao, Carregando, cx } from '../components/ui'
import { PromptInstalar } from '../components/PromptInstalar'
import { documentoFormatado, telefoneFormatado } from '../lib/formato'
import { ROTULO_PAPEL, ROTULO_RAMO } from '../lib/rotulos'
import type { Ramo } from '../types/dominio'

export function PaginaConfig() {
  const { sessao } = useAuth()
  const oficina = sessao?.oficina
  const [ramoAberto, setRamoAberto] = useState<Ramo | undefined>(oficina?.ramos?.[0])

  const { data: usuarios } = useQuery({ queryKey: ['usuarios'], queryFn: sessaoApi.usuarios })
  const { data: template, isLoading } = useQuery({
    queryKey: ['template', ramoAberto],
    queryFn: () => templatesApi.porRamo(ramoAberto!),
    enabled: !!ramoAberto,
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina titulo="Configurações" />

      <Cartao>
        <div className="mb-2 flex flex-wrap items-center justify-between gap-3">
          <h2 className="font-bold">{oficina?.nomeFantasia}</h2>
          <PromptInstalar />
        </div>
        <dl className="space-y-1 text-sm">
          {oficina?.cnpj && (
            <div className="flex justify-between">
              <dt className="text-tinta-500">CNPJ</dt>
              <dd>{documentoFormatado(oficina.cnpj)}</dd>
            </div>
          )}
          {oficina?.telefone && (
            <div className="flex justify-between">
              <dt className="text-tinta-500">Telefone</dt>
              <dd>{telefoneFormatado(oficina.telefone)}</dd>
            </div>
          )}
          {oficina?.horarioFuncionamento && (
            <div className="flex justify-between gap-3">
              <dt className="shrink-0 text-tinta-500">Horário</dt>
              <dd className="text-right">{oficina.horarioFuncionamento}</dd>
            </div>
          )}
          <div className="flex justify-between gap-3">
            <dt className="shrink-0 text-tinta-500">Ramos</dt>
            <dd className="text-right">
              {oficina?.ramos?.map((r) => ROTULO_RAMO[r]).join(' · ')}
            </dd>
          </div>
        </dl>
      </Cartao>

      <section>
        <h2 className="mb-2 font-bold">Campos da OS por ramo</h2>
        <p className="mb-2 text-sm text-tinta-500">
          Estes são os campos que aparecem ao abrir uma OS. Editar cria uma nova versão — as OS
          antigas continuam com o formato original.
        </p>

        <div className="mb-3 flex flex-wrap gap-2">
          {oficina?.ramos?.map((ramo) => (
            <button
              key={ramo}
              onClick={() => setRamoAberto(ramo)}
              className={cx(
                'min-h-11 rounded-xl border px-3 text-sm font-semibold',
                ramoAberto === ramo
                  ? 'border-white/70 bg-white text-black shadow-[var(--tk-glow)]'
                  : 'border-white/10 bg-white/[0.04] text-tinta-700 hover:border-white/25 hover:text-tinta-900',
              )}
            >
              {ROTULO_RAMO[ramo]}
            </button>
          ))}
        </div>

        {isLoading ? (
          <Carregando />
        ) : template ? (
          <Cartao>
            <div className="mb-3 flex items-center justify-between">
              <p className="text-sm font-semibold">
                {template.campos.length} campos · versão {template.versao}
              </p>
            </div>
            <ul className="space-y-2">
              {[...template.campos]
                .sort((a, b) => a.ordem - b.ordem)
                .map((campo) => (
                  <li
                    key={campo.chave}
                    className="flex items-start justify-between gap-3 border-b border-white/6 pb-2 text-sm last:border-0"
                  >
                    <div className="min-w-0">
                      <p className="truncate font-medium">
                        {campo.rotulo}
                        {campo.obrigatorio && <span className="ml-1 text-red-400">*</span>}
                      </p>
                      <p className="text-xs text-tinta-400">
                        {campo.chave} · {campo.grupo}
                        {campo.condicional && ` · depende de ${campo.condicional.campo}`}
                      </p>
                    </div>
                    <span className="shrink-0 rounded bg-white/[0.07] px-2 py-0.5 font-mono text-xs text-tinta-600">
                      {campo.tipo}
                    </span>
                  </li>
                ))}
            </ul>
          </Cartao>
        ) : null}
      </section>

      <section>
        <h2 className="mb-2 font-bold">Equipe</h2>
        <Cartao>
          <ul className="space-y-2">
            {usuarios?.map((u) => (
              <li key={u.id} className="flex items-center justify-between gap-3 text-sm">
                <div className="min-w-0">
                  <p className="truncate font-medium">{u.nome}</p>
                  <p className="truncate text-xs text-tinta-400">{u.email}</p>
                </div>
                <span className="shrink-0 rounded-full border border-white/10 bg-white/[0.06] px-2.5 py-0.5 text-xs font-bold text-tinta-700">
                  {ROTULO_PAPEL[u.papel]}
                </span>
              </li>
            ))}
          </ul>
        </Cartao>
      </section>
    </div>
  )
}
