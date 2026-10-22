import { Link, useParams } from 'react-router'
import ChargeActions from '../charges/ChargeActions.jsx'
import ChargeFacts from '../charges/ChargeFacts.jsx'
import ChargeTimeline from '../charges/ChargeTimeline.jsx'
import { chargeStatus } from '../charges/chargeStatus.js'
import { formatCents } from '../charges/money.js'
import { useCharge } from '../charges/useCharge.js'
import Badge from '../components/Badge.jsx'
import Card from '../components/Card.jsx'
import LoadError from '../components/LoadError.jsx'
import styles from './ChargeDetailPage.module.css'

function ChargeDetailPage() {
  const { id } = useParams()
  const { charge, error, loading, update, reload } = useCharge(id)

  function renderContent() {
    if (loading) {
      return <p className={styles.muted}>Carregando a cobrança…</p>
    }
    // Um id que nem tem formato de UUID dá 400, e para quem chegou por um link quebrado é o mesmo que não existir.
    // Nos dois casos, tentar de novo não muda nada, então a página explica em vez de oferecer o botão.
    if (error?.status === 404 || error?.status === 400) {
      return (
        <Card title="Cobrança não encontrada">
          <p className={styles.muted}>
            Ela não existe ou é de outro lojista. A API responde do mesmo jeito nos dois casos, para não revelar nem
            que a cobrança de outra loja existe.
          </p>
        </Card>
      )
    }
    if (error) {
      return <LoadError message="Não deu para carregar a cobrança." error={error} onRetry={reload} />
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
