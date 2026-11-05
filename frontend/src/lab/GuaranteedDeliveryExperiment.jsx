import { useState } from 'react'
import { Link } from 'react-router'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import { useConsole } from '../console/useConsole.js'
import { findWebhookEndpoint } from './findWebhookEndpoint.js'
import GuaranteedDeliveryRun from './GuaranteedDeliveryRun.jsx'
import styles from './Experiment.module.css'

const NO_TEST_RECEIVER = 'Esta API não tem o receptor de teste. Ele só existe quando ela roda com o profile demo.'

function GuaranteedDeliveryExperiment() {
  const { highlight } = useConsole()
  const [run, setRun] = useState(null)
  const [needsReceiver, setNeedsReceiver] = useState(false)
  const [checking, setChecking] = useState(false)
  const [error, setError] = useState(null)

  // A conferência só roda no clique, para o experimento parado não consultar nada. Se o receptor já estiver fora do
  // ar, como depois de um experimento abandonado no meio, a primeira etapa já começa concluída.
  async function start() {
    const tag = crypto.randomUUID()
    highlight(tag)
    setChecking(true)
    setRun(null)
    setNeedsReceiver(false)
    setError(null)
    try {
      const receiver = await api.get('/dev/webhook-receiver', { tag })
      const endpoint = await findWebhookEndpoint(tag)
      if (endpoint?.url === receiver.url) {
        setRun({ tag, startsOffline: receiver.offline })
      } else {
        setNeedsReceiver(true)
      }
    } catch (caught) {
      setError(caught.status === 404 ? NO_TEST_RECEIVER : caught.message)
    } finally {
      setChecking(false)
    }
  }

  return (
    <Card title="Entrega garantida">
      <p className={styles.description}>
        Deixa o receptor de teste fora do ar, cria uma cobrança e acompanha a API insistindo no webhook. O esperado é
        cada tentativa falhar com 503 e, com o receptor de volta, o evento chegar uma vez só. Os pedidos de cada etapa
        ficam destacados no console.
      </p>
      <Button variant="secondary" onClick={start} disabled={checking}>
        {checking ? 'Conferindo…' : run ? 'Recomeçar' : 'Começar'}
      </Button>

      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
      {needsReceiver && (
        <p role="alert" className={styles.error}>
          O experimento precisa do receptor de teste cadastrado como webhook.{' '}
          <Link to="/webhooks">Cadastrar na página de webhooks</Link>
        </p>
      )}

      {run && <GuaranteedDeliveryRun tag={run.tag} startsOffline={run.startsOffline} />}
    </Card>
  )
}

export default GuaranteedDeliveryExperiment
