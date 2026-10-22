// Aceita o valor como se escreve no Brasil: "150", "150,5", "1.500,00". O ponto só vale como separador de milhar.
const REAIS_PATTERN = /^(\d{1,3}(?:\.\d{3})+|\d+)(?:,(\d{1,2}))?$/

// Converte o texto digitado em reais para centavos, ou devolve null quando o texto não é um valor. A conta é feita
// juntando os dígitos do texto, sem passar por um número com casas decimais: "1.500,5" vira "1500" + "50".
export function parseReaisToCents(text) {
  const match = REAIS_PATTERN.exec(text.trim())
  if (!match) {
    return null
  }
  const [, integerPart, fraction = ''] = match
  const cents = Number(integerPart.replaceAll('.', '') + fraction.padEnd(2, '0'))
  return Number.isSafeInteger(cents) ? cents : null
}

// Só para exibir. A divisão por 100 nunca volta para a API, que recebe e devolve centavos.
export function formatCents(cents, currency) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency }).format(cents / 100)
}
