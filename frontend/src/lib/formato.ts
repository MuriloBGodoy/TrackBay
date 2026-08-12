/** Formatacao e mascaras pt-BR: R$, dd/MM/yyyy, virgula decimal. */

const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const NUMERO = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const DATA = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeZone: 'America/Sao_Paulo' })
const DATA_HORA = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: 'America/Sao_Paulo',
})

export function moeda(valor?: number | null): string {
  return MOEDA.format(valor ?? 0)
}

export function numero(valor?: number | null): string {
  return NUMERO.format(valor ?? 0)
}

export function data(iso?: string | null): string {
  if (!iso) return '—'
  return DATA.format(new Date(iso))
}

export function dataHora(iso?: string | null): string {
  if (!iso) return '—'
  return DATA_HORA.format(new Date(iso))
}

/** "há 2 dias", "em 3 horas" — usado nas listas de OS. */
export function tempoRelativo(iso?: string | null): string {
  if (!iso) return '—'
  const rtf = new Intl.RelativeTimeFormat('pt-BR', { numeric: 'auto' })
  const diff = new Date(iso).getTime() - Date.now()
  const minutos = Math.round(diff / 60000)
  if (Math.abs(minutos) < 60) return rtf.format(minutos, 'minute')
  const horas = Math.round(minutos / 60)
  if (Math.abs(horas) < 24) return rtf.format(horas, 'hour')
  const dias = Math.round(horas / 24)
  if (Math.abs(dias) < 30) return rtf.format(dias, 'day')
  return data(iso)
}

export function placaFormatada(placa?: string | null): string {
  if (!placa) return '—'
  const p = placa.replace(/[^A-Za-z0-9]/g, '').toUpperCase()
  // So o padrao antigo leva hifen; Mercosul (ABC1D23) fica direto.
  if (/^[A-Z]{3}\d{4}$/.test(p)) return `${p.slice(0, 3)}-${p.slice(3)}`
  return p
}

export function documentoFormatado(doc?: string | null): string {
  if (!doc) return '—'
  const d = doc.replace(/\D/g, '')
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
  return doc
}

export function telefoneFormatado(tel?: string | null): string {
  if (!tel) return '—'
  const d = tel.replace(/\D/g, '')
  if (d.length === 11) return d.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
  if (d.length === 10) return d.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3')
  return tel
}

/** Mascara aplicada enquanto o usuario digita. */
export function mascararPlaca(valor: string): string {
  return valor.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 7)
}

export function mascararTelefone(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 11)
  if (d.length <= 2) return d
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
}

export function mascararCpf(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 11)
  return d
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

export function mascararCnpj(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 14)
  return d
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1/$2')
    .replace(/(\d{4})(\d{1,2})$/, '$1-$2')
}

export function mascararCep(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 8)
  return d.replace(/(\d{5})(\d)/, '$1-$2')
}

/** Converte "1.234,56" digitado pelo usuario em 1234.56. */
export function paraNumero(valor: string): number {
  const limpo = valor.replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, '')
  const n = Number(limpo)
  return Number.isFinite(n) ? n : 0
}
