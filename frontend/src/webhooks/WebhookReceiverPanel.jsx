import { useState } from 'react'
import { api } from '../api/client.js'
import { usePolling } from '../api/usePolling.js'
import Card from '../components/Card.jsx'
import LoadError from '../components/LoadError.jsx'
import Switch from '../components/Switch.jsx'
import ReceivedWebhookItem from './ReceivedWebhookItem.jsx'
import styles from './WebhookReceiverPanel.module.css'

function WebhookReceiverPanel({ registeredUrl }) {
  const { data: receiver, error, loading, reload } = usePolling('/dev/webhook-receiver')
  const [pending, setPending] = useState(false)
  const [switchError, setSwitchError] = useState(null)
  const busy = pending || loading

  // O reload recomeça as consultas e descarta a que estiver a caminho. Ela pode ter saído antes da troca, e a resposta
  // dela faria o interruptor voltar atrás por um instante.
  async function setOffline(offline) {
    if (busy) {
      return
    }
    setPending(true)
    setSwitchError(null)
    try {
      await api.put('/dev/webhook-receiver/offline', { body: { offline } })
      reload()
    } catch (caught) {
      setSwitchError(caught.message)
    } finally {
      setPending(false)
    }
  }

  // Sem o profile demo, a rota não existe e o painel não tem o que mostrar.
  if (error?.status === 404 || (!receiver && !error)) {
    return null
  }
  if (!receiver) {
    return (
      <Card title="Receptor de teste">
        <LoadError message="Não deu para carregar o receptor." error={error} onRetry={reload} />
      </Card>
    )
  }

  return (
    <Card title="Receptor de teste">
      <p className={styles.muted}>
        Faz o papel do servidor da loja. Fora do ar, ele responde 503, e a Ponte tenta entregar de novo mais tarde. O
        que chega fica só na memória da API e some quando ela reinicia.
      </p>
      {error && (
        <p role="status" className={styles.stale}>
          O painel pode estar desatualizado. {error.message} A página continua tentando.
        </p>
      )}
      <Switch
        label="Fora do ar"
        checked={receiver.offline}
        aria-disabled={busy}
        onChange={(event) => setOffline(event.target.checked)}
      />
      {switchError && (
        <p role="alert" className={styles.error}>
          {switchError}
        </p>
      )}
      {receiver.received.length === 0 ? (
        <p className={styles.muted}>
          Nada chegou ainda.
          {registeredUrl !== receiver.url && ' O webhook cadastrado aponta para outra URL, e só esta recebe aqui.'}
        </p>
      ) : (
        <ul className={styles.list}>
          {receiver.received.map((webhook) => (
            <ReceivedWebhookItem key={`${webhook.receivedAt}:${webhook.eventId}`} webhook={webhook} />
          ))}
        </ul>
      )}
    </Card>
  )
}

export default WebhookReceiverPanel
