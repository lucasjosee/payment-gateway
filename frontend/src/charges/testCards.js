// Cada cartão de teste leva a um resultado diferente no antifraude do backend (pacote charge/fraud).
// Sem number, o número é sorteado a cada uso. A CardVelocityRule recusa o mesmo cartão a partir da quarta cobrança
// em 5 minutos, então um número fixo deixaria de ser aprovado depois de poucos testes.
export const TEST_CARDS = [
  { id: 'approved', label: 'Aprovado', currency: 'BRL' },
  // A TestCardDeclineRule recusa todo cartão com final 0002, como os cartões de recusa dos gateways reais.
  { id: 'declined', label: 'Recusado (final 0002)', currency: 'BRL', number: '4000000000000002' },
  // A CurrencyRule só aceita BRL, e ela roda antes das outras regras.
  { id: 'usd', label: 'Moeda USD', currency: 'USD' },
]

export function testCardFields(testCard) {
  return {
    currency: testCard.currency,
    // Validade alguns anos à frente, para os cartões de teste nunca vencerem.
    card: { number: testCard.number ?? randomCardNumber(), expiry: `${new Date().getFullYear() + 3}-12`, cvv: '123' },
  }
}

// Um número de 16 dígitos começando com 4, como os da Visa. O último dígito é o verificador de Luhn, a mesma conta
// que o @CreditCardNumber confere no backend antes de aceitar o número.
function randomCardNumber() {
  let digits = '4'
  for (let i = 0; i < 14; i++) {
    digits += Math.floor(Math.random() * 10)
  }
  return digits + luhnCheckDigit(digits)
}

// Da direita para a esquerda, um dígito sim, outro não é dobrado, começando pelo que vai ficar ao lado do
// verificador. Quando o dobro passa de 9, somam-se os dois algarismos dele, o que dá o mesmo que subtrair 9.
function luhnCheckDigit(digits) {
  let sum = 0
  for (let position = 0; position < digits.length; position++) {
    let digit = Number(digits[digits.length - 1 - position])
    if (position % 2 === 0) {
      digit *= 2
      if (digit > 9) {
        digit -= 9
      }
    }
    sum += digit
  }
  return (10 - (sum % 10)) % 10
}
