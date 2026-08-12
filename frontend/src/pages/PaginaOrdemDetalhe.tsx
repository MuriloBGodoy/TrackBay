import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft } from '@phosphor-icons/react'
import { ordensApi, templatesApi } from '../api/recursos'
import { useAuth } from '../auth/AuthContext'
import { MotorCampos } from '../forms/MotorCampos'
import {
  BarraAcao,
  Botao,
  Cartao,
  Carregando,
  ErroBox,
  EtiquetaPagamento,
  EtiquetaStatus,
  ROTULO_STATUS,
} from '../components/ui'
import { dataHora, moeda, placaFormatada } from '../lib/formato'
import { ROTULO_FORMA_PAGAMENTO, ROTULO_RAMO } from '../lib/rotulos'
import type { StatusOS } from '../types/dominio'

export function PaginaOrdemDetalhe() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()
  const { sessao } = useAuth()
  const [erro, setErro] = useState<string>()
  const [gerandoPdf, setGerandoPdf] = useState(false)

  const { data: os, isLoading } = useQuery({
    queryKey: ['ordem', id],
    queryFn: () => ordensApi.porId(id),
  })

  // Busca a versao do schema com que a OS foi criada, nao a vigente.
  const { data: template } = useQuery({
    queryKey: ['template', os?.ramo, os?.schemaVersion],
    queryFn: () => templatesApi.porRamo(os!.ramo, os!.schemaVersion),
    enabled: !!os,
  })

  const { data: transicoes } = useQuery({
    queryKey: ['transicoes', id, os?.status],
    queryFn: () => ordensApi.transicoes(id),
    enabled: !!os,
  })

  const mudarStatus = useMutation({
    mutationFn: (status: StatusOS) => ordensApi.mudarStatus(id, status),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['ordem', id] })
      void queryClient.invalidateQueries({ queryKey: ['ordens'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      setErro(undefined)
    },
    onError: (e: Error) => setErro(e.message),
  })

  if (isLoading || !os) return <Carregando />

  // No celular tenta compartilhar o arquivo (WhatsApp etc.); senao abre em nova aba.
  const abrirPdf = async () => {
    setGerandoPdf(true)
    try {
      const blob = await ordensApi.pdf(id)
      const nome = `${os.numero.replace(/[^\w-]/g, '-')}.pdf`
      const arquivo = new File([blob], nome, { type: 'application/pdf' })
      if (navigator.canShare?.({ files: [arquivo] })) {
        await navigator.share({ files: [arquivo], title: os.numero })
        return
      }
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (e) {
      if ((e as Error).name !== 'AbortError') setErro((e as Error).message)
    } finally {
      setGerandoPdf(false)
    }
  }

  const compartilharWhatsApp = () => {
    const linhas = [
      `*${sessao?.oficina?.nomeFantasia}*`,
      `OS ${os.numero}`,
      `Veículo: ${placaFormatada(os.veiculoPlaca)} — ${os.veiculoDescricao ?? ''}`,
      '',
      ...os.itensServico.map((i) => `• ${i.descricao} — ${moeda(i.total ?? i.valorUnitario * i.quantidade)}`),
      ...os.itensPeca.map((i) => `• ${i.descricao} (${i.quantidade}x) — ${moeda(i.total)}`),
      '',
      `*Total: ${moeda(os.total)}*`,
      os.textoGarantia ? `\n_${os.textoGarantia}_` : '',
    ]
    const texto = encodeURIComponent(linhas.filter(Boolean).join('\n'))
    window.open(`https://wa.me/?text=${texto}`, '_blank')
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <Link
            to="/app/ordens"
            className="mb-1.5 inline-flex items-center gap-1.5 text-xs font-semibold text-tinta-500 transition-colors hover:text-tinta-900"
          >
            <ArrowLeft size={13} weight="bold" />
            Ordens de serviço
          </Link>
          <div className="flex flex-wrap items-center gap-2.5">
            <h1 className="font-display text-2xl font-bold tracking-tight text-tinta-900">
              {os.numero}
            </h1>
            <EtiquetaStatus status={os.status} />
          </div>
          <p className="mt-1 text-sm text-tinta-500">Aberta em {dataHora(os.dataAbertura)}</p>
        </div>
      </div>

      {erro && <ErroBox mensagem={erro} />}

      <Cartao>
        <div className="flex items-center justify-between">
          <div>
            <p className="font-mono text-lg font-bold">{placaFormatada(os.veiculoPlaca)}</p>
            <p className="text-sm text-tinta-600">{os.veiculoDescricao}</p>
            <p className="mt-1 text-sm text-tinta-500">{os.clienteNome}</p>
          </div>
          {os.kmEntrada && (
            <div className="text-right">
              <p className="text-xs text-tinta-500">Km entrada</p>
              <p className="font-bold">{os.kmEntrada.toLocaleString('pt-BR')}</p>
            </div>
          )}
        </div>
      </Cartao>

      {(os.reclamacaoCliente || os.diagnosticoTecnico) && (
        <Cartao className="space-y-3">
          {os.reclamacaoCliente && (
            <div>
              <p className="text-xs font-bold text-tinta-500 uppercase">Reclamação do cliente</p>
              <p className="mt-1 text-sm">{os.reclamacaoCliente}</p>
            </div>
          )}
          {os.diagnosticoTecnico && (
            <div>
              <p className="text-xs font-bold text-tinta-500 uppercase">Diagnóstico técnico</p>
              <p className="mt-1 text-sm">{os.diagnosticoTecnico}</p>
            </div>
          )}
        </Cartao>
      )}

      {template && Object.keys(os.camposDinamicos).length > 0 && (
        <Cartao>
          <div className="mb-3">
            <h2 className="font-bold">Detalhes do serviço</h2>
            <p className="text-xs text-tinta-500">
              {ROTULO_RAMO[os.ramo]} · schema v{os.schemaVersion}
            </p>
          </div>
          <MotorCampos
            campos={template.campos}
            valores={os.camposDinamicos}
            aoMudar={() => {}}
            somenteLeitura
          />
        </Cartao>
      )}

      <Cartao>
        <h2 className="mb-3 font-bold">Itens</h2>
        <ul className="space-y-2">
          {os.itensServico.map((item, i) => (
            <li key={i} className="flex justify-between gap-3 text-sm">
              <span className="min-w-0">
                <span className="block truncate">{item.descricao}</span>
                {item.mecanicoNome && (
                  <span className="text-xs text-tinta-400">{item.mecanicoNome}</span>
                )}
              </span>
              <span className="shrink-0 font-semibold">{moeda(item.total)}</span>
            </li>
          ))}
          {os.itensPeca.map((item, i) => (
            <li key={`p${i}`} className="flex justify-between gap-3 text-sm">
              <span className="min-w-0 truncate">
                {item.descricao} <span className="text-tinta-400">({item.quantidade}x)</span>
              </span>
              <span className="shrink-0 font-semibold">{moeda(item.total)}</span>
            </li>
          ))}
        </ul>

        <dl className="mt-4 space-y-1 border-t border-white/8 pt-3 text-sm">
          <div className="flex justify-between text-tinta-500">
            <dt>Serviços</dt>
            <dd>{moeda(os.subtotalServicos)}</dd>
          </div>
          <div className="flex justify-between text-tinta-500">
            <dt>Peças</dt>
            <dd>{moeda(os.subtotalPecas)}</dd>
          </div>
          {!!os.descontoGeral && (
            <div className="flex justify-between text-tinta-500">
              <dt>Desconto</dt>
              <dd>− {moeda(os.descontoGeral)}</dd>
            </div>
          )}
          <div className="flex justify-between pt-1 text-base font-black">
            <dt>Total</dt>
            <dd>{moeda(os.total)}</dd>
          </div>
        </dl>
      </Cartao>

      {!!os.pagamento?.parcelas?.length && (
        <Cartao>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-bold">Pagamento</h2>
            <EtiquetaPagamento status={os.pagamento.status} />
          </div>
          <ul className="space-y-2">
            {os.pagamento.parcelas.map((p, i) => (
              <li key={i} className="flex justify-between text-sm">
                <span>
                  {ROTULO_FORMA_PAGAMENTO[p.forma]}
                  {p.numeroParcelas && p.numeroParcelas > 1 && ` em ${p.numeroParcelas}x`}
                </span>
                <span className="font-semibold">{moeda(p.valor)}</span>
              </li>
            ))}
          </ul>
        </Cartao>
      )}

      {!!os.historicoStatus?.length && (
        <Cartao>
          <h2 className="mb-3 font-bold">Histórico</h2>
          <ol className="space-y-2">
            {os.historicoStatus.map((t, i) => (
              <li key={i} className="flex gap-3 text-sm">
                <span className="mt-1.5 size-2 shrink-0 rounded-full bg-tinta-500" />
                <div>
                  <p>
                    {ROTULO_STATUS[t.de]} → <strong>{ROTULO_STATUS[t.para]}</strong>
                  </p>
                  <p className="text-xs text-tinta-400">
                    {t.autorNome} · {dataHora(t.em)}
                    {t.observacao && ` · ${t.observacao}`}
                  </p>
                </div>
              </li>
            ))}
          </ol>
        </Cartao>
      )}

      <BarraAcao>
        <Botao variante="secundario" tamanho="grande" disabled={gerandoPdf} onClick={() => void abrirPdf()}>
          {gerandoPdf ? '…' : 'PDF'}
        </Botao>
        <Botao variante="secundario" tamanho="grande" onClick={compartilharWhatsApp}>
          WhatsApp
        </Botao>
        {transicoes?.permitidos?.map((status) => (
          <Botao
            key={status}
            tamanho="grande"
            variante={status === 'CANCELADA' ? 'perigo' : 'primario'}
            disabled={mudarStatus.isPending}
            onClick={() => mudarStatus.mutate(status)}
            className="flex-1"
          >
            {ROTULO_STATUS[status]}
          </Botao>
        ))}
      </BarraAcao>
    </div>
  )
}
