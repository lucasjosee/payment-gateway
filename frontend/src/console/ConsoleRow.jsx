import { statusLabel, statusTone } from './entryStatus.js'
import styles from './ConsoleRow.module.css'

// Com milissegundos, para dar para ver a ordem de pedidos disparados juntos, como no laboratório.
const TIME_FORMAT = new Intl.DateTimeFormat('pt-BR', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  fractionalSecondDigits: 3,
})

function ConsoleRow({ entry }) {
  return (
    <tr className={styles.row}>
      <td className={styles.muted}>{TIME_FORMAT.format(new Date(entry.startedAt))}</td>
      <td className={styles.method}>{entry.method}</td>
      <td title={entry.path}>{entry.path}</td>
      <td className={styles[statusTone(entry)]}>{statusLabel(entry)}</td>
      <td className={styles.duration}>{entry.durationMs === null ? '…' : `${entry.durationMs} ms`}</td>
      {/* Os 8 primeiros caracteres bastam para reconhecer o pedido. O ID inteiro aparece ao passar o mouse. */}
      <td className={styles.muted} title={entry.requestId ?? undefined}>
        {entry.requestId ? entry.requestId.slice(0, 8) : '—'}
      </td>
    </tr>
  )
}

export default ConsoleRow
