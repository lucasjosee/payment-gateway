import Badge from '../components/Badge.jsx'
import ReceivedWebhookItem from '../webhooks/ReceivedWebhookItem.jsx'
import styles from './Experiment.module.css'

// A prova vem do receptor, e não só do status da entrega: o evento precisa estar lá, uma vez só e com a assinatura boa.
function GuaranteedDeliveryResult({ delivery, arrivals }) {
  const asExpected = arrivals.length === 1 && arrivals[0].signatureValid

  return (
    <>
      <p>
        {asExpected ? (
          <Badge tone="success">Como esperado</Badge>
        ) : (
          <Badge tone="danger">Resultado inesperado</Badge>
        )}
      </p>
      <p>
        {asExpected
          ? `O evento chegou uma vez, com a assinatura válida, na tentativa ${delivery.attempts}. As falhas não fizeram ele se perder nem chegar repetido.`
          : `A entrega consta como entregue, mas o receptor tem ${arrivals.length} eventos com o id dela, e ele guarda tudo só em memória.`}
      </p>
      <ul className={styles.arrivals}>
        {arrivals.map((webhook) => (
          <ReceivedWebhookItem key={webhook.receivedAt} webhook={webhook} />
        ))}
      </ul>
    </>
  )
}

export default GuaranteedDeliveryResult
