import { declineReasonText } from './chargeStatus.js'
import { formatDateTime } from './dateTime.js'
import { formatCents } from './money.js'
import styles from './ChargeFacts.module.css'

// Os dados de uma cobrança em pares de nome e valor. Aparece no resultado da nova cobrança e na página de detalhe.
function ChargeFacts({ charge }) {
  return (
    <dl className={styles.facts}>
      <dt>Valor</dt>
      <dd>{formatCents(charge.amountCents, charge.currency)}</dd>
      <dt>Cartão</dt>
      <dd>•••• {charge.cardLast4}</dd>
      {charge.declineReason && (
        <>
          <dt>Motivo</dt>
          <dd>
            {declineReasonText(charge.declineReason)} <code className={styles.code}>{charge.declineReason}</code>
          </dd>
        </>
      )}
      <dt>Criada em</dt>
      <dd>{formatDateTime(charge.createdAt)}</dd>
      <dt>ID</dt>
      <dd>
        <code className={styles.code}>{charge.id}</code>
      </dd>
    </dl>
  )
}

export default ChargeFacts
