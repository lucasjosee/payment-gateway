import { chargeStatus } from './chargeStatus.js'
import { formatDateTime } from './dateTime.js'
import styles from './ChargeTimeline.module.css'

function eventTitle(event) {
  const to = chargeStatus(event.toStatus).label
  return event.fromStatus === null ? `Criada como ${to}` : `${chargeStatus(event.fromStatus).label} → ${to}`
}

// Os eventos chegam da API do mais antigo para o mais novo. O primeiro não tem estado de origem: é a criação.
function ChargeTimeline({ events }) {
  return (
    <ol className={styles.timeline}>
      {events.map((event) => (
        <li
          key={`${event.toStatus}-${event.createdAt}`}
          className={`${styles.event} ${styles[chargeStatus(event.toStatus).tone]}`}
        >
          <p className={styles.title}>{eventTitle(event)}</p>
          <time className={styles.time} dateTime={event.createdAt}>
            {formatDateTime(event.createdAt)}
          </time>
        </li>
      ))}
    </ol>
  )
}

export default ChargeTimeline
