import { useState } from 'react'
import { api } from '../api/client.js'
import { usePolling } from '../api/usePolling.js'
import Button from '../components/Button.jsx'
import WebhookDeliveryTable from '../webhooks/WebhookDeliveryTable.jsx'
import ExperimentStep from './ExperimentStep.jsx'
import GuaranteedDeliveryResult from './GuaranteedDeliveryResult.jsx'
import { testChargeBody } from './testChargeBody.js'
import styles from './Experiment.module.css'

const AMOUNT_CENTS = 3000
// Duas falhas bastam para mostrar a espera entre uma tentativa e outra.
const FAILURES_TO_SHOW = 2

function isFinished({ items }) {
  return items.length > 0 && items[0].status !== 'PENDING'
}

// A entrega só fica DELIVERED depois de o receptor responder, então basta uma leitura dele para achar o evento.
function readOnce() {
  return true
}

// Uma API reiniciada no meio do experimento volta com o receptor ligado, e a entrega chega sem ninguém religar. Por
// isso a entrega concluída também avança para a última etapa.
function currentStep({ offline, charge, delivery, arrivals }) {
  if (!charge) {
    return offline ? 2 : 1
  }
  if (!offline || delivery?.status === 'DELIVERED') {
    return arrivals ? 6 : 5
  }
  return delivery?.attempts >= FAILURES_TO_SHOW ? 4 : 3
}

function GuaranteedDeliveryRun({ tag, startsOffline }) {
  const [offline, setOffline] = useState(startsOffline)
  const [charge, setCharge] = useState(null)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const deliveries = usePolling(charge ? `/webhook-deliveries?chargeId=${charge.id}` : null, { until: isFinished, tag })
  const delivery = deliveries.data?.items[0]
  const receiver = usePolling(delivery?.status === 'DELIVERED' ? '/dev/webhook-receiver' : null, {
    until: readOnce,
    tag,
  })
  const arrivals = receiver.data?.received.filter((webhook) => webhook.eventId === delivery.id)
  const failed = delivery?.status === 'FAILED'

  const current = currentStep({ offline, charge, delivery, arrivals })
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

  function setReceiverOffline(value) {
    return perform(async () => {
      const updated = await api.put('/dev/webhook-receiver/offline', { body: { offline: value }, tag })
      setOffline(updated.offline)
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
            <Button variant="secondary" onClick={() => setReceiverOffline(true)} disabled={pending}>
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

        <ExperimentStep number={4} title="Religar o receptor" status={statusOf(4)}>
          {!failed && (
            <p className={styles.description}>
              Ninguém precisa pedir o envio de novo: a entrega continua guardada no banco, e a API tenta outra vez no
              horário marcado na tabela.
            </p>
          )}
          {current === 4 && (
            <Button variant="secondary" onClick={() => setReceiverOffline(false)} disabled={pending}>
              Religar
            </Button>
          )}
        </ExperimentStep>

        <ExperimentStep number={5} title="Ver o evento chegar" status={statusOf(5)}>
          {arrivals ? (
            <GuaranteedDeliveryResult delivery={delivery} arrivals={arrivals} />
          ) : (
            !failed && <p className={styles.description}>Aguardando a próxima tentativa da API.</p>
          )}
          {receiver.error && (
            <p role="status" className={styles.error}>
              Não deu para consultar o receptor. {receiver.error.message} A página continua tentando.
            </p>
          )}
        </ExperimentStep>
      </ol>

      {failed && (
        <p role="alert" className={styles.error}>
          As tentativas acabaram antes de o evento chegar, e a API não vai insistir mais nesta entrega. Recomece o
          experimento.
        </p>
      )}

      {error && (
        <p role="alert" className={styles.error}>
          {error}
        </p>
      )}
    </>
  )
}

export default GuaranteedDeliveryRun
