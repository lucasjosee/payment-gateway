import { Link } from 'react-router'
import Badge from '../components/Badge.jsx'
import Card from '../components/Card.jsx'
import ChargeFacts from './ChargeFacts.jsx'
import { chargeStatus } from './chargeStatus.js'

// Uma cobrança recusada também é uma resposta de sucesso da API (201): ela foi criada, só que no estado DECLINED.
function ChargeResult({ charge }) {
  const { label, tone } = chargeStatus(charge.status)

  return (
    <Card title="Última cobrança">
      <Badge tone={tone}>{label}</Badge>
      <ChargeFacts charge={charge} />
      <p>
        <Link to={`/cobrancas/${charge.id}`}>Ver detalhe da cobrança</Link>
      </p>
    </Card>
  )
}

export default ChargeResult
