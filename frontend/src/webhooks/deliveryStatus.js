// As chaves são os valores do enum WebhookDeliveryStatus do backend.
const STATUSES = {
  PENDING: { label: 'Pendente', tone: 'warning' },
  DELIVERED: { label: 'Entregue', tone: 'success' },
  FAILED: { label: 'Falhou', tone: 'danger' },
}

// Um valor que a tela ainda não conhece aparece como veio da API, em vez de sumir.
export function deliveryStatus(status) {
  return STATUSES[status] ?? { label: status, tone: 'neutral' }
}
