import { useState } from 'react'
import { api } from '../api/client.js'
import Badge from '../components/Badge.jsx'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import { useConsole } from '../console/useConsole.js'
import { testChargeBody } from './testChargeBody.js'
import styles from './Experiment.module.css'
import comparison from './IdempotencyExperiment.module.css'

const AMOUNT_CENTS = 5000

function IdempotencyExperiment() {
  const { highlight } = useConsole()
  const [run, setRun] = useState(null)
  const [running, setRunning] = useState(false)
  const [error, setError] = useState(null)

  async function start() {
    const idempotencyKey = crypto.randomUUID()
    const tag = crypto.randomUUID()
    const body = testChargeBody(AMOUNT_CENTS)
    highlight(tag)
    setRunning(true)
    setRun(null)
    setError(null)
    try {
      // Um envio depois do outro, como um cliente que repete o pedido porque não chegou a ver a primeira resposta.
      const first = await api.post('/charges', { body, idempotencyKey, tag })
      const second = await api.post('/charges', { body, idempotencyKey, tag })
      setRun({ idempotencyKey, first, second })
    } catch (caught) {
      setError(caught)
    } finally {
      setRunning(false)
    }
  }

  const identical = run !== null && JSON.stringify(run.first) === JSON.stringify(run.second)

  return (
    <Card title="Idempotência">
      <p className={styles.description}>
        Cria uma cobrança e, logo depois, envia o mesmo pedido de novo com a mesma Idempotency-Key. O esperado é a API
        criar uma cobrança só e devolver a mesma resposta nas duas vezes. Os dois pedidos ficam destacados no console.
      </p>
      <Button variant="secondary" onClick={start} disabled={running}>
        {running ? 'Enviando…' : 'Enviar duas vezes'}
      </Button>

      {error && (
        <p role="alert" className={styles.error}>
          {error.message}
        </p>
      )}

      {run && (
        <>
          <p>
            {identical ? (
              <Badge tone="success">Respostas idênticas</Badge>
            ) : (
              <Badge tone="danger">Respostas diferentes</Badge>
            )}
          </p>
          <p>
            {identical
              ? `As duas respostas trazem a mesma cobrança, ${run.first.id.slice(0, 8)}. Ela foi criada uma vez só, e o segundo envio recebeu a resposta guardada.`
              : 'As respostas não batem: o segundo envio foi tratado como um pedido novo.'}{' '}
            Idempotency-Key usada nos dois: <code className={styles.code}>{run.idempotencyKey}</code>
          </p>
          <div className={comparison.columns}>
            <section>
              <h3 className={comparison.title}>1º envio</h3>
              <pre className={comparison.body}>{JSON.stringify(run.first, null, 2)}</pre>
            </section>
            <section>
              <h3 className={comparison.title}>2º envio</h3>
              <pre className={comparison.body}>{JSON.stringify(run.second, null, 2)}</pre>
            </section>
          </div>
        </>
      )}
    </Card>
  )
}

export default IdempotencyExperiment
