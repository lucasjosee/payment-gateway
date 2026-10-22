import { useState } from 'react'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import { chargeStatus } from './chargeStatus.js'
import styles from './ChargeActions.module.css'

// Espelha as transições do enum ChargeStatus no backend, para habilitar só os botões que valem no estado atual.
const ACTIONS = [
  { path: 'capture', label: 'Capturar', pendingLabel: 'Capturando…', from: 'AUTHORIZED', variant: 'primary' },
  { path: 'cancel', label: 'Cancelar', pendingLabel: 'Cancelando…', from: 'AUTHORIZED', variant: 'secondary' },
  { path: 'refund', label: 'Estornar', pendingLabel: 'Estornando…', from: 'CAPTURED', variant: 'secondary' },
]

function ChargeActions({ charge, onUpdated }) {
  const [demoMode, setDemoMode] = useState(false)
  const [pendingAction, setPendingAction] = useState(null)
  const [error, setError] = useState(null)

  const hasAvailableAction = ACTIONS.some((action) => action.from === charge.status)

  async function run(action) {
    setPendingAction(action.path)
    setError(null)
    try {
      // Cada clique é uma tentativa nova, com uma Idempotency-Key nova. Enquanto ela não volta, os botões ficam
      // desabilitados, então um clique duplo não vira dois pedidos.
      onUpdated(await api.post(`/charges/${charge.id}/${action.path}`, { idempotent: true }))
    } catch (caught) {
      setError(caught)
    } finally {
      setPendingAction(null)
    }
  }

  return (
    <Card title="Ações">
      <div className={styles.buttons}>
        {ACTIONS.map((action) => (
          <Button
            key={action.path}
            variant={action.variant}
            onClick={() => run(action)}
            disabled={pendingAction !== null || (!demoMode && action.from !== charge.status)}
          >
            {pendingAction === action.path ? action.pendingLabel : action.label}
          </Button>
        ))}
      </div>

      {!hasAvailableAction && !demoMode && (
        <p className={styles.muted}>
          Uma cobrança {chargeStatus(charge.status).label.toLowerCase()} não aceita mais nenhuma ação.
        </p>
      )}

      {error && (
        <div role="alert" className={styles.error}>
          <p>
            <strong>{error.status === 0 ? 'Sem resposta da API' : `Erro ${error.status}`}</strong>: {error.message}
          </p>
          {error.requestId && <p className={styles.requestId}>Request ID {error.requestId}</p>}
        </div>
      )}

      <div className={styles.demo}>
        <Button variant="ghost" size="sm" aria-pressed={demoMode} onClick={() => setDemoMode(!demoMode)}>
          Modo demonstração
        </Button>
        <p className={styles.muted}>
          {demoMode
            ? 'Todos os botões ficam liberados. Nas transições que a máquina de estados não permite, a API responde 409.'
            : 'Libera todos os botões, em qualquer estado, para ver como a API responde.'}
        </p>
      </div>
    </Card>
  )
}

export default ChargeActions
