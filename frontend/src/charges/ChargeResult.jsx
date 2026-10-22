import { Link } from 'react-router'
import Badge from '../components/Badge.jsx'
import Card from '../components/Card.jsx'
import { chargeStatus, declineReasonText } from './chargeStatus.js'
import { formatCents } from './money.js'
import styles from './ChargeResult.module.css'

// Uma cobrança recusada também é uma resposta de sucesso da API (201): ela foi criada, só que no estado DECLINED.
function ChargeResult({ charge }) {
  const { label, tone } = chargeStatus(charge.status)

  return (
    <Card title="Última cobrança">
      <Badge tone={tone}>{label}</Badge>
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
        <dt>ID</dt>
        <dd>
          <code className={styles.code}>{charge.id}</code>
        </dd>
      </dl>
      <p>
        <Link to={`/cobrancas/${charge.id}`}>Ver detalhe da cobrança</Link>
      </p>
    </Card>
  )
}

export default ChargeResult
