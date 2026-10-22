import { useState } from 'react'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import { useConsole } from '../console/useConsole.js'
import ConcurrencyScoreboard from './ConcurrencyScoreboard.jsx'
import { testChargeBody } from './testChargeBody.js'
import styles from './Experiment.module.css'

const AMOUNT_CENTS = 7500
const MIN_ATTEMPTS = 2
const MAX_ATTEMPTS = 50

function ConcurrencyExperiment() {
  const { highlight } = useConsole()
  const [attempts, setAttempts] = useState('20')
  const [result, setResult] = useState(null)
  const [running, setRunning] = useState(false)
  const [error, setError] = useState(null)

  async function start(event) {
    event.preventDefault()
    const count = Number(attempts)
    const tag = crypto.randomUUID()
    setRunning(true)
    setResult(null)
    setError(null)
    try {
      const charge = await api.post('/charges', { body: testChargeBody(AMOUNT_CENTS), idempotent: true })
      highlight(tag)

      const startedAt = performance.now()
      // Cada api.post dispara o pedido no momento em que é chamado, e nenhum espera o anterior terminar. O Promise.all
      // só aguarda todos voltarem. Cada pedido transforma o próprio erro num resultado, porque o Promise.all desiste
      // no primeiro pedido que falha, e o placar precisa de todos.
      const outcomes = await Promise.all(
        Array.from({ length: count }, () =>
          api
            .post(`/charges/${charge.id}/capture`, { idempotent: true, tag })
            .then(() => ({ ok: true }))
            .catch((caught) => ({ ok: false, status: caught.status, message: caught.message })),
        ),
      )
      const durationMs = Math.round(performance.now() - startedAt)

      // A prova final vem do banco: por mais pedidos que tenham chegado, a cobrança precisa ter uma captura só.
      const finalCharge = await api.get(`/charges/${charge.id}`)
      setResult({ outcomes, durationMs, finalCharge })
    } catch (caught) {
      setError(caught)
    } finally {
      setRunning(false)
    }
  }

  return (
    <Card title="Concorrência">
      <p className={styles.description}>
        Cria uma cobrança autorizada e dispara várias capturas dela ao mesmo tempo, cada uma com a sua Idempotency-Key.
        O esperado é uma captura dar certo e todas as outras receberem 409. As capturas ficam destacadas no console.
      </p>
      <form className={styles.form} onSubmit={start}>
        <Input
          label="Capturas simultâneas"
          type="number"
          value={attempts}
          onChange={(event) => setAttempts(event.target.value)}
          min={MIN_ATTEMPTS}
          max={MAX_ATTEMPTS}
          required
        />
        <Button type="submit" variant="secondary" disabled={running}>
          {running ? 'Disparando…' : 'Disparar capturas'}
        </Button>
      </form>

      {error && (
        <p role="alert" className={styles.error}>
          {error.message}
        </p>
      )}

      {result && <ConcurrencyScoreboard {...result} />}
    </Card>
  )
}

export default ConcurrencyExperiment
