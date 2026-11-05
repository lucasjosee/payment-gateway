import { formatDateTime } from '../charges/dateTime.js'
import Badge from '../components/Badge.jsx'
import styles from './WebhookReceiverPanel.module.css'

// O POST do receptor é público, então o corpo pode ser qualquer coisa. O tipo é conferido porque um objeto no lugar
// do texto derrubaria a página inteira na hora de mostrar.
function readBody(body) {
  try {
    const parsed = JSON.parse(body)
    return {
      eventType: typeof parsed?.type === 'string' ? parsed.type : null,
      formatted: JSON.stringify(parsed, null, 2),
    }
  } catch {
    return { eventType: null, formatted: body }
  }
}

function ReceivedWebhookItem({ webhook }) {
  const { eventType, formatted } = readBody(webhook.body)

  return (
    <li>
      <details>
        <summary className={styles.summary}>
          <span className={styles.muted}>{formatDateTime(webhook.receivedAt)}</span>
          <span className={styles.event}>{eventType ?? 'Evento desconhecido'}</span>
          <Badge tone={webhook.signatureValid ? 'success' : 'danger'}>
            Assinatura {webhook.signatureValid ? 'válida' : 'inválida'}
          </Badge>
        </summary>
        <pre className={styles.body}>{formatted}</pre>
      </details>
    </li>
  )
}

export default ReceivedWebhookItem
