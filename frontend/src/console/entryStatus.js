// Como o console classifica cada requisição. O 4xx tem cor própria, separada do 5xx, porque no gateway ele quase
// sempre é uma regra de negócio barrando o pedido de propósito (409 da máquina de estados, 422 da idempotência),
// e não algo quebrado.

export function isError(entry) {
  return entry.error !== null || entry.status >= 400
}

export function statusTone(entry) {
  if (entry.error !== null || entry.status >= 500) {
    return 'danger'
  }
  if (entry.status === null) {
    return 'pending'
  }
  if (entry.status >= 400) {
    return 'warning'
  }
  return 'success'
}

export function statusLabel(entry) {
  if (entry.error !== null) {
    return 'falhou'
  }
  return entry.status ?? '…'
}
