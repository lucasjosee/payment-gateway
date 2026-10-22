// Como os estados e os motivos de recusa do backend aparecem na tela. As chaves são os valores que a API devolve:
// os do enum ChargeStatus e os códigos das regras do antifraude.
const STATUSES = {
  AUTHORIZED: { label: 'Autorizada', tone: 'info' },
  DECLINED: { label: 'Recusada', tone: 'danger' },
  CAPTURED: { label: 'Capturada', tone: 'success' },
  CANCELED: { label: 'Cancelada', tone: 'neutral' },
  REFUNDED: { label: 'Estornada', tone: 'neutral' },
}

const DECLINE_REASONS = {
  CURRENCY_NOT_SUPPORTED: 'Moeda não aceita. O gateway só cobra em reais.',
  CARD_DECLINED: 'Recusada pelo banco emissor.',
  AMOUNT_ABOVE_LIMIT: 'Valor acima do limite de R$ 10.000,00.',
  CARD_VELOCITY_EXCEEDED: 'Cartão usado muitas vezes em poucos minutos.',
}

// Um valor que a tela ainda não conhece aparece como veio da API, em vez de sumir.
export function chargeStatus(status) {
  return STATUSES[status] ?? { label: status, tone: 'neutral' }
}

export function declineReasonText(reason) {
  return DECLINE_REASONS[reason] ?? reason
}
