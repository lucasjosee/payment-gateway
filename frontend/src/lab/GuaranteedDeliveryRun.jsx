import { useState } from 'react'
import { api } from '../api/client.js'
import { usePolling } from '../api/usePolling.js'
import Button from '../components/Button.jsx'
import WebhookDeliveryTable from '../webhooks/WebhookDeliveryTable.jsx'
import ExperimentStep from './ExperimentStep.jsx'
import { testChargeBody } from './testChargeBody.js'
import styles from './Experiment.module.css'

const AMOUNT_CENTS = 3000

function isFinished({ items }) {
  return items.length > 0 && items[0].status !== 'PENDING'
}

function currentStep({ offline, charge }) {
  if (!charge) {
    return offline ? 2 : 1
  }
  return 3
}

function GuaranteedDeliveryRun({ tag, startsOffline }) {
  const [offline, setOffline] = useState(startsOffline)
  const [charge, setCharge] = useState(null)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const deliveries = usePolling(charge ? `/webhook-deliveries?chargeId=${charge.id}` : null, { until: isFinished, tag })
  const delivery = deliveries.data?.items[0]

  const current = currentStep({ offline, charge })
  const statusOf = (number) => (number < current ? 'done' : number === current ? 'current' : 'waiting')

  async function perform(action) {
    setPending(true)
    setError(null)
    try {
      await action()
    } catch (caught) {
      setError(caught.message)
    } finally {
      setPending(false)
    }
  }

  function takeReceiverOffline() {
    return perform(async () => {
      const receiver = await api.put('/dev/webhook-receiver/offline', { body: { offline: true }, tag })
      setOffline(receiver.offline)
    })
  }

  function createCharge() {
    return perform(async () => {
      setCharge(await api.post('/charges', { body: testChargeBody(AMOUNT_CENTS), idempotent: true, tag }))
    })
  }

  return (
    <>
      <ol className={styles.steps}>
        <ExperimentStep number={1} title="Deixar o receptor fora do ar" status={statusOf(1)}>
          <p className={styles.description}>
            Ele passa a responder 503, como um servidor de loja que caiu. Se você sair no meio do experimento, ele
            continua assim até ser religado na página de webhooks.
          </p>
          {current === 1 && (
            <Button variant="secondary" onClick={takeReceiverOffline} disabled={pending}>
              Tirar do ar
            </Button>
          )}
        </ExperimentStep>

        <ExperimentStep number={2} title="Criar uma cobrança" status={statusOf(2)}>
          <p className={styles.description}>
            O webhook é gravado na mesma transação da cobrança, então a API não depende de o servidor da loja estar no
            ar.
          </p>
          {current === 2 && (
            <Button variant="secondary" onClick={createCharge} disabled={pending}>
              Criar cobrança
            </Button>
          )}
        </ExperimentStep>

        <ExperimentStep number={3} title="Acompanhar as tentativas falhando" status={statusOf(3)}>
          <p className={styles.description}>
            A API tenta de novo sozinha, com esperas cada vez maiores. Cada tentativa também aparece na aba Servidor do
            console.
          </p>
          {delivery && <WebhookDeliveryTable deliveries={[delivery]} />}
          {deliveries.error && (
            <p role="status" className={styles.error}>
              Não deu para consultar a entrega. {deliveries.error.message} A página continua tentando.
            </p>
          )}
        </ExperimentStep>
      </ol>

      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
    </>
  )
}

export default GuaranteedDeliveryRun
