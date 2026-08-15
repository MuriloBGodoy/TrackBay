import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from '@phosphor-icons/react'
import { clientesApi, veiculosApi } from '../api/recursos'
import {
  Botao,
  CabecalhoPagina,
  Campo,
  Cartao,
  Carregando,
  ErroBox,
  EtiquetaStatus,
  Input,
  Selecao,
  Vazio,
} from '../components/ui'
import { mascararPlaca, moeda, paraNumero, placaFormatada, tempoRelativo } from '../lib/formato'
import { ROTULO_CAMBIO, ROTULO_COMBUSTIVEL, ROTULO_TIPO_VEICULO } from '../lib/rotulos'
import type { Cambio, Combustivel, TipoVeiculo, Veiculo } from '../types/dominio'

export function PaginaVeiculos() {
  const [params] = useSearchParams()
  const [busca, setBusca] = useState(params.get('busca') ?? '')
  const [selecionado, setSelecionado] = useState<string>()
  const [criando, setCriando] = useState(false)

  const { data: veiculos, isLoading } = useQuery({
    queryKey: ['veiculos', busca],
    queryFn: () => veiculosApi.listar({ busca: busca || undefined }),
  })

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Veículos"
        descricao="Busque pela placa para abrir o histórico do carro."
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
        placeholder="Buscar por placa, marca ou modelo"
        autoFocus
      />

      {criando && <FormularioVeiculo aoFechar={() => setCriando(false)} />}

      {isLoading ? (
        <Carregando />
      ) : !veiculos?.length ? (
        <Cartao>
          <Vazio
            titulo="Nenhum veículo encontrado"
            descricao="Tente outra placa ou cadastre o veículo em “Novo”."
          />
        </Cartao>
      ) : (
        <ul className="space-y-2">
          {veiculos.map((v) => (
            <li key={v.id}>
              <Cartao>
                <button
                  onClick={() => setSelecionado(selecionado === v.id ? undefined : v.id)}
                  className="flex w-full items-start justify-between gap-3 text-left"
                >
                  <div className="min-w-0">
                    <p className="font-mono text-lg font-bold">{placaFormatada(v.placa)}</p>
                    <p className="truncate text-sm text-tinta-600">
                      {v.marca} {v.modelo} {v.anoModelo && `· ${v.anoModelo}`}
                    </p>
                    <p className="text-xs text-tinta-400">
                      {v.kmAtual ? `${v.kmAtual.toLocaleString('pt-BR')} km · ` : ''}
                      {ROTULO_TIPO_VEICULO[v.tipoVeiculo]}
                    </p>
                  </div>
                  <span className="text-sm text-tinta-600">
                    {selecionado === v.id ? 'fechar' : 'histórico'}
                  </span>
                </button>

                {selecionado === v.id && <HistoricoVeiculo veiculoId={v.id} />}
              </Cartao>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

/**
 * Cadastro de veiculo. Obrigatorios de verdade sao so tres — proprietario,
 * placa e tipo —, que e o que o backend cobra (`VeiculoService.salvar`). O resto
 * fica opcional para o carro entrar no patio agora e a ficha ser completada
 * depois; quem esta no balcao com o cliente na frente nao tem tempo de preencher
 * dezoito campos.
 */
function FormularioVeiculo({ aoFechar }: { aoFechar: () => void }) {
  const queryClient = useQueryClient()
  const [clienteId, setClienteId] = useState('')
  const [placa, setPlaca] = useState('')
  const [tipoVeiculo, setTipoVeiculo] = useState<TipoVeiculo>('CARRO')
  const [marca, setMarca] = useState('')
  const [modelo, setModelo] = useState('')
  const [anoModelo, setAnoModelo] = useState('')
  const [cor, setCor] = useState('')
  const [kmAtual, setKmAtual] = useState('')
  const [combustivel, setCombustivel] = useState<Combustivel | ''>('')
  const [cambio, setCambio] = useState<Cambio | ''>('')
  const [erro, setErro] = useState<string>()

  // A lista inteira: uma oficina tem centenas de clientes, nao milhares, e o
  // select nativo do celular ja resolve a busca.
  const { data: clientes, isLoading: carregandoClientes } = useQuery({
    queryKey: ['clientes', ''],
    queryFn: () => clientesApi.listar(),
  })

  const criar = useMutation({
    mutationFn: veiculosApi.criar,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['veiculos'] })
      aoFechar()
    },
    onError: (e: Error) => setErro(e.message),
  })

  const enviar = (e: React.FormEvent) => {
    e.preventDefault()
    setErro(undefined)
    if (!clienteId) {
      setErro('Escolha o proprietário do veículo.')
      return
    }
    const veiculo: Partial<Veiculo> = {
      clienteId,
      placa,
      tipoVeiculo,
      marca: marca || undefined,
      modelo: modelo || undefined,
      anoModelo: anoModelo ? paraNumero(anoModelo) : undefined,
      cor: cor || undefined,
      kmAtual: kmAtual ? paraNumero(kmAtual) : undefined,
      combustivel: combustivel || undefined,
      cambio: cambio || undefined,
      ativo: true,
    }
    criar.mutate(veiculo)
  }

  return (
    <Cartao>
      <form onSubmit={enviar} className="space-y-3">
        {erro && <ErroBox mensagem={erro} />}

        <Campo rotulo="Proprietário" obrigatorio>
          <Selecao value={clienteId} onChange={(e) => setClienteId(e.target.value)}>
            <option value="">
              {carregandoClientes ? 'Carregando clientes…' : 'Selecione o cliente'}
            </option>
            {clientes?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </Selecao>
        </Campo>

        {!carregandoClientes && !clientes?.length && (
          <p className="text-xs text-tinta-500">
            Nenhum cliente cadastrado ainda —{' '}
            <Link to="/app/clientes" className="underline underline-offset-2 hover:text-tinta-900">
              cadastre o cliente primeiro
            </Link>
            .
          </p>
        )}

        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Placa" obrigatorio dica="Padrão antigo ou Mercosul">
            <Input
              value={placa}
              onChange={(e) => setPlaca(mascararPlaca(e.target.value))}
              placeholder="ABC1D23"
              autoCapitalize="characters"
              autoFocus
              className="font-mono"
            />
          </Campo>

          <Campo rotulo="Tipo" obrigatorio>
            <Selecao
              value={tipoVeiculo}
              onChange={(e) => setTipoVeiculo(e.target.value as TipoVeiculo)}
            >
              {(Object.keys(ROTULO_TIPO_VEICULO) as TipoVeiculo[]).map((t) => (
                <option key={t} value={t}>
                  {ROTULO_TIPO_VEICULO[t]}
                </option>
              ))}
            </Selecao>
          </Campo>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Marca">
            <Input value={marca} onChange={(e) => setMarca(e.target.value)} placeholder="Fiat" />
          </Campo>
          <Campo rotulo="Modelo">
            <Input value={modelo} onChange={(e) => setModelo(e.target.value)} placeholder="Uno" />
          </Campo>
        </div>

        <div className="grid grid-cols-3 gap-3">
          <Campo rotulo="Ano">
            <Input
              value={anoModelo}
              inputMode="numeric"
              onChange={(e) => setAnoModelo(e.target.value.replace(/\D/g, '').slice(0, 4))}
              placeholder="2019"
            />
          </Campo>
          <Campo rotulo="Cor">
            <Input value={cor} onChange={(e) => setCor(e.target.value)} placeholder="Prata" />
          </Campo>
          <Campo rotulo="Km">
            <Input
              value={kmAtual}
              inputMode="numeric"
              onChange={(e) => setKmAtual(e.target.value.replace(/\D/g, ''))}
              placeholder="98000"
            />
          </Campo>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Combustível">
            <Selecao
              value={combustivel}
              onChange={(e) => setCombustivel(e.target.value as Combustivel | '')}
            >
              <option value="">Não informado</option>
              {(Object.keys(ROTULO_COMBUSTIVEL) as Combustivel[]).map((c) => (
                <option key={c} value={c}>
                  {ROTULO_COMBUSTIVEL[c]}
                </option>
              ))}
            </Selecao>
          </Campo>
          <Campo rotulo="Câmbio">
            <Selecao value={cambio} onChange={(e) => setCambio(e.target.value as Cambio | '')}>
              <option value="">Não informado</option>
              {(Object.keys(ROTULO_CAMBIO) as Cambio[]).map((c) => (
                <option key={c} value={c}>
                  {ROTULO_CAMBIO[c]}
                </option>
              ))}
            </Selecao>
          </Campo>
        </div>

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

/** Historico completo da placa: toda OS que aquele carro ja teve. */
function HistoricoVeiculo({ veiculoId }: { veiculoId: string }) {
  const { data: historico, isLoading } = useQuery({
    queryKey: ['historico', veiculoId],
    queryFn: () => veiculosApi.historico(veiculoId),
  })

  if (isLoading) return <Carregando texto="Buscando histórico…" />

  if (!historico?.length) {
    return (
      <p className="mt-3 border-t border-white/8 pt-3 text-sm text-tinta-500">
        Nenhuma OS registrada para este veículo.
      </p>
    )
  }

  return (
    <ul className="mt-3 space-y-2 border-t border-white/8 pt-3">
      {historico.map((os) => (
        <li key={os.id}>
          <Link to={`/app/ordens/${os.id}`} className="flex items-center justify-between gap-2 text-sm">
            <span className="min-w-0">
              <span className="font-semibold">{os.numero}</span>
              <span className="ml-2 text-tinta-400">{tempoRelativo(os.dataAbertura)}</span>
            </span>
            <span className="flex shrink-0 items-center gap-2">
              <EtiquetaStatus status={os.status} />
              <span className="font-semibold">{moeda(os.total)}</span>
            </span>
          </Link>
        </li>
      ))}
    </ul>
  )
}
