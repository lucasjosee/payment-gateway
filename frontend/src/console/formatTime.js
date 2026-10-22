const TIME_FORMAT = new Intl.DateTimeFormat('pt-BR', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  fractionalSecondDigits: 3,
})

export function formatTime(isoString) {
  return TIME_FORMAT.format(new Date(isoString))
}
