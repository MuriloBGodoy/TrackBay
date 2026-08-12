import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { clientesApi, ordensApi, templatesApi, veiculosApi } from '../api/recursos'
import { useAuth } from '../auth/AuthContext'
import { MotorCampos, validarCampos } from '../forms/MotorCampos'
import {
  AreaTexto,
  BarraAcao,
  Botao,
  CabecalhoPagina,
  Campo,
  Cartao,
  ErroBox,
  Input,
  Selecao,
  cx,
} from '../components/ui'
import { moeda, placaFormatada } from '../lib/formato'
import { ROTULO_RAMO } from '../lib/rotulos'
import type { ItemServico, Ramo } from '../types/dominio'

const PASSOS = ['Cliente e veículo', 'Detalhes do serviço', 'Serviços e valores']

/**
 * Abertura de OS em 3 passos, pensada para o balcao:
 * quem esta atendendo escolhe o veiculo, descreve o problema e lanca os itens.
 */
export function PaginaNovaOrdem() {
  const navigate = useNavigate()
  const { sessao } = useAuth()
  const ramosDaOficina = sessao?.oficina?.ramos ?? []

  const [passo, setPasso] = useState(1)
  const [clienteId, setClienteId] = useState('')
  const [veiculoId, setVeiculoId] = useState('')
  const [ramo, setRamo] = useState<Ramo>(ramosDaOficina[0] ?? 'OUTRO')
  const [kmEntrada, setKmEntrada] = useState('')
  const [reclamacao, setReclamacao] = useState('')
  const [camposDinamicos, setCamposDinamicos] = useState<Record<string, unknown>>({})
  const [itens, setItens] = useState<ItemServico[]>([])
  const [erro, setErro] = useState<string>()
  const [errosCampos, setErrosCampos] = useState<Record<string, string>>({})

  const { data: clientes } = useQuery({ queryKey: ['clientes'], queryFn: () => clientesApi.listar() })
  const { data: veiculos } = useQuery({
    queryKey: ['veiculos', clienteId],
    queryFn: () => veiculosApi.listar({ clienteId }),
    enabled: !!clienteId,
  })
  const { data: template } = useQuery({
    queryKey: ['template', ramo],
    queryFn: () => templatesApi.porRamo(ramo),
    enabled: !!ramo,
  })

  const veiculo = veiculos?.find((v) => v.id === veiculoId)

  const total = useMemo(
    () => itens.reduce((soma, i) => soma + (i.valorUnitario || 0) * (i.quantidade || 0), 0),
    [itens],
  )

  const criar = useMutation({
    mutationFn: ordensApi.criar,
    onSuccess: (os) => navigate(`/app/ordens/${os.id}`, { replace: true }),
    onError: (e: Error) => setErro(e.message),
  })

  const mudarCampo = (chave: string, valor: unknown) => {
    setCamposDinamicos((atual) => ({ ...atual, [chave]: valor }))
    setErrosCampos((atual) => {
      const { [chave]: _, ...resto } = atual
      return resto
    })
  }

  const adicionarItem = () =>
    setItens((atual) => [...atual, { descricao: '', valorUnitario: 0, quantidade: 1 }])

  const mudarItem = (indice: number, dados: Partial<ItemServico>) =>
    setItens((atual) => atual.map((item, i) => (i === indice ? { ...item, ...dados } : item)))

  const removerItem = (indice: number) =>
    setItens((atual) => atual.filter((_, i) => i !== indice))

  const salvar = () => {
    setErro(undefined)
    if (!clienteId || !veiculoId) {
      setPasso(1)
      return setErro('Escolha o cliente e o veículo')
    }
    // Valida no front com o mesmo schema que o backend cobra: erro aparece antes do request.
    if (template) {
      const erros = validarCampos(template.campos, camposDinamicos, veiculo?.tipoVeiculo)
      if (Object.keys(erros).length) {
        setErrosCampos(erros)
        setPasso(2)
        return setErro('Preencha os campos obrigatórios do serviço')
      }
    }

    criar.mutate({
      clienteId,
      veiculoId,
      ramo,
      kmEntrada: kmEntrada ? Number(kmEntrada) : undefined,
      reclamacaoCliente: reclamacao || undefined,
      camposDinamicos,
      itensServico: itens.filter((i) => i.descricao.trim()),
      itensPeca: [],
    })
  }

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        sobretitulo="Ordem de serviço"
        titulo="Nova OS"
        descricao="Três passos: quem é o carro, o que o ramo pede e o que vai ser cobrado."
      />

      {/* Trilha clicavel: da para voltar a um passo sem perder o que ja foi digitado. */}
      <div className="flex gap-2">
        {PASSOS.map((rotulo, i) => {
          const numero = i + 1
          return (
            <button
              key={rotulo}
              type="button"
              onClick={() => setPasso(numero)}
              aria-current={passo === numero ? 'step' : undefined}
              className="group min-w-0 flex-1 text-left"
            >
              <span
                className={cx(
                  'block h-1.5 rounded-full transition-colors',
                  passo >= numero ? 'bg-white' : 'bg-white/12 group-hover:bg-white/25',
                )}
              />
              <span
                className={cx(
                  'mt-1.5 block truncate text-[11px] font-semibold transition-colors',
                  passo >= numero ? 'text-tinta-800' : 'text-tinta-500',
                )}
              >
                {numero}. {rotulo}
              </span>
            </button>
          )
        })}
      </div>

      {erro && <ErroBox mensagem={erro} />}

      {passo === 1 && (
        <Cartao className="space-y-4">
          <h2 className="font-bold">1. Cliente e veículo</h2>

          <Campo rotulo="Cliente" obrigatorio>
            <Selecao
              value={clienteId}
              onChange={(e) => {
                setClienteId(e.target.value)
                setVeiculoId('')
              }}
            >
              <option value="">Selecione…</option>
              {clientes?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nome}
                </option>
              ))}
            </Selecao>
          </Campo>

          {clienteId && (
            <Campo rotulo="Veículo" obrigatorio>
              <Selecao value={veiculoId} onChange={(e) => setVeiculoId(e.target.value)}>
                <option value="">Selecione…</option>
                {veiculos?.map((v) => (
                  <option key={v.id} value={v.id}>
                    {placaFormatada(v.placa)} — {v.marca} {v.modelo}
                  </option>
                ))}
              </Selecao>
            </Campo>
          )}

          {ramosDaOficina.length > 1 && (
            <Campo rotulo="Ramo do serviço" dica="Define quais campos aparecem no próximo passo">
              <Selecao value={ramo} onChange={(e) => setRamo(e.target.value as Ramo)}>
                {ramosDaOficina.map((r) => (
                  <option key={r} value={r}>
                    {ROTULO_RAMO[r]}
                  </option>
                ))}
              </Selecao>
            </Campo>
          )}

          <Campo rotulo="Km de entrada">
            <Input
              type="number"
              inputMode="numeric"
              value={kmEntrada}
              onChange={(e) => setKmEntrada(e.target.value)}
              placeholder={veiculo?.kmAtual ? String(veiculo.kmAtual) : '0'}
            />
          </Campo>

          <Campo rotulo="Reclamação do cliente" dica="O que o cliente relatou, nas palavras dele">
            <AreaTexto
              rows={3}
              value={reclamacao}
              onChange={(e) => setReclamacao(e.target.value)}
              placeholder="Ex.: Carro esquentando no trânsito"
            />
          </Campo>
        </Cartao>
      )}

      {passo === 2 && (
        <Cartao className="space-y-4">
          <div>
            <h2 className="font-bold">2. Detalhes do serviço</h2>
            <p className="text-xs text-tinta-500">
              Campos do ramo {template ? ROTULO_RAMO[template.ramo] : '—'} · versão{' '}
              {template?.versao}
            </p>
          </div>
          {template && (
            <MotorCampos
              campos={template.campos}
              valores={camposDinamicos}
              aoMudar={mudarCampo}
              tipoVeiculo={veiculo?.tipoVeiculo}
              erros={errosCampos}
            />
          )}
        </Cartao>
      )}

      {passo === 3 && (
        <Cartao className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold">3. Serviços</h2>
            <Botao variante="secundario" onClick={adicionarItem}>
              + Item
            </Botao>
          </div>

          {itens.length === 0 && (
            <p className="py-4 text-center text-sm text-tinta-500">
              Nenhum serviço lançado. Você pode adicionar depois.
            </p>
          )}

          {itens.map((item, i) => (
            <div key={i} className="space-y-2 rounded-xl border border-white/10 bg-white/[0.02] p-3">
              <Campo rotulo="Descrição">
                <Input
                  value={item.descricao}
                  onChange={(e) => mudarItem(i, { descricao: e.target.value })}
                  placeholder="Ex.: Limpeza química do radiador"
                />
              </Campo>
              <div className="grid grid-cols-3 gap-2">
                <Campo rotulo="Valor">
                  <Input
                    type="number"
                    inputMode="decimal"
                    step="0.01"
                    value={item.valorUnitario || ''}
                    onChange={(e) => mudarItem(i, { valorUnitario: Number(e.target.value) })}
                  />
                </Campo>
                <Campo rotulo="Qtd">
                  <Input
                    type="number"
                    inputMode="numeric"
                    value={item.quantidade || ''}
                    onChange={(e) => mudarItem(i, { quantidade: Number(e.target.value) })}
                  />
                </Campo>
                <div className="flex items-end">
                  <Botao variante="fantasma" onClick={() => removerItem(i)} className="w-full">
                    Remover
                  </Botao>
                </div>
              </div>
            </div>
          ))}

          <div className="flex justify-between border-t border-tinta-200 pt-3">
            <span className="font-bold">Total</span>
            <span className="text-lg font-black">{moeda(total)}</span>
          </div>
        </Cartao>
      )}

      <BarraAcao>
        {passo > 1 && (
          <Botao variante="secundario" tamanho="grande" onClick={() => setPasso(passo - 1)} className="flex-1">
            Voltar
          </Botao>
        )}
        {passo < 3 ? (
          <Botao tamanho="grande" onClick={() => setPasso(passo + 1)} className="flex-1">
            Continuar
          </Botao>
        ) : (
          <Botao tamanho="grande" onClick={salvar} disabled={criar.isPending} className="flex-1">
            {criar.isPending ? 'Salvando…' : 'Abrir OS'}
          </Botao>
        )}
      </BarraAcao>
    </div>
  )
}
