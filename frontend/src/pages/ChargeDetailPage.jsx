import { Link, useParams } from 'react-router'
import ChargeActions from '../charges/ChargeActions.jsx'
import ChargeFacts from '../charges/ChargeFacts.jsx'
import ChargeTimeline from '../charges/ChargeTimeline.jsx'
import { chargeStatus } from '../charges/chargeStatus.js'
import { formatCents } from '../charges/money.js'
import { useCharge } from '../charges/useCharge.js'
import Badge from '../components/Badge.jsx'
import Card from '../components/Card.jsx'
import styles from './ChargeDetailPage.module.css'

function ChargeDetailPage() {
  const { id } = useParams()
  const { charge, error, loading, update } = useCharge(id)

  function renderContent() {
    if (loading) {
      return <p className={styles.muted}>Carregando a cobrança…</p>
    }
    // Uma cobrança de outro lojista também dá 404: a API não conta nem que ela existe.
    if (error) {
      return (
        <p role="alert" className={styles.error}>
          {error.message}
        </p>
      )
    }

    const { label, tone } = chargeStatus(charge.status)
    return (
      <>
        <div className={styles.header}>
          <h1>{formatCents(charge.amountCents, charge.currency)}</h1>
          <Badge tone={tone}>{label}</Badge>
        </div>
        <div className={styles.layout}>
          <div className={styles.column}>
            <Card title="Dados">
              <ChargeFacts charge={charge} />
            </Card>
            <Card title="Linha do tempo">
              <ChargeTimeline events={charge.events} />
            </Card>
          </div>
          <ChargeActions charge={charge} onUpdated={update} />
        </div>
      </>
    )
  }

  return (
    <>
      <Link to="/cobrancas" className={styles.back}>
        ← Cobranças
      </Link>
      {renderContent()}
    </>
  )
}

export default ChargeDetailPage
