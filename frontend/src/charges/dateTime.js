// Com segundos, para dar para conferir a ordem de cobranças e de eventos que acontecem em sequência.
const DATE_TIME_FORMAT = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'medium' })

export function formatDateTime(isoString) {
  return DATE_TIME_FORMAT.format(new Date(isoString))
}
