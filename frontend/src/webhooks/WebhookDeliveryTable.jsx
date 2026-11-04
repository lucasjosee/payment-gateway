import { Link } from 'react-router'
import { formatDateTime } from '../charges/dateTime.js'
import Badge from '../components/Badge.jsx'
import styles from '../components/DataTable.module.css'
import { deliveryStatus } from './deliveryStatus.js'

// Sem status HTTP, ou a primeira tentativa ainda não aconteceu, ou o servidor da loja não respondeu.
function lastHttpStatusText({ attempts, lastHttpStatus }) {
  return lastHttpStatus ?? (attempts > 0 ? 'Sem resposta' : '—')
}

function WebhookDeliveryTable({ deliveries }) {
  return (
    <div className={styles.scroll}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">Evento</th>
            <th scope="col">Cobrança</th>
            <th scope="col">Status</th>
            <th scope="col">Tentativas</th>
            <th scope="col">Último status HTTP</th>
            <th scope="col">Próxima tentativa</th>
          </tr>
        </thead>
        <tbody>
          {deliveries.map((delivery) => {
            const { label, tone } = deliveryStatus(delivery.status)
            return (
              <tr key={delivery.id}>
                <td>{delivery.eventType}</td>
                <td>
                  <Link to={`/cobrancas/${delivery.chargeId}`} className={styles.id} title={delivery.chargeId}>
                    {delivery.chargeId.slice(0, 8)}
                  </Link>
                </td>
                <td>
                  <Badge tone={tone}>{label}</Badge>
                </td>
                <td>{delivery.attempts}</td>
                <td className={styles.muted}>{lastHttpStatusText(delivery)}</td>
                {/* A API só informa a próxima tentativa enquanto a entrega está pendente. */}
                <td className={styles.muted}>
                  {delivery.nextAttemptAt ? formatDateTime(delivery.nextAttemptAt) : '—'}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default WebhookDeliveryTable
