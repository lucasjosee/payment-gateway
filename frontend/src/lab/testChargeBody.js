import { TEST_CARDS, testCardFields } from '../charges/testCards.js'

const APPROVED_CARD = TEST_CARDS.find((testCard) => testCard.id === 'approved')

// O corpo de uma cobrança que o antifraude aprova. O cartão é sorteado a cada chamada, como na tela de nova cobrança,
// para os experimentos poderem rodar várias vezes seguidas sem esbarrar no limite de uso do mesmo cartão.
export function testChargeBody(amountCents) {
  const { currency, card } = testCardFields(APPROVED_CARD)
  return { amountCents, currency, card }
}
