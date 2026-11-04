import { useState } from 'react'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import styles from './WebhookEndpointForm.module.css'

const NO_TEST_RECEIVER = 'Esta API não tem o receptor de teste. Ele só existe quando ela roda com o profile demo.'

// O botão do receptor só preenche o campo. Esta linha mostra que a URL ainda não foi salva, para ninguém achar que
// o cadastro já aconteceu.
function registrationStatus(registeredUrl, url) {
  if (registeredUrl === null) {
    return 'Nenhuma URL cadastrada ainda.'
  }
  if (url.trim() === registeredUrl) {
    return 'Esta é a URL cadastrada.'
  }
  return `Esta URL ainda não foi salva. A cadastrada continua sendo ${registeredUrl}`
}

// O campo não é type="url" para quem decide ser a API: o navegador barraria o pedido antes, com uma mensagem dele.
function WebhookEndpointForm({ registeredUrl, onRegistered }) {
  const [url, setUrl] = useState(registeredUrl ?? '')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      onRegistered(await api.put('/webhook-endpoint', { body: { url: url.trim() } }))
    } catch (caught) {
      setError(caught.problem?.errors?.[0]?.message ?? caught.message)
    } finally {
      setPending(false)
    }
  }

  async function fillWithTestReceiver() {
    setPending(true)
    setError(null)
    try {
      const receiver = await api.get('/dev/webhook-receiver')
      setUrl(receiver.url)
    } catch (caught) {
      setError(caught.status === 404 ? NO_TEST_RECEIVER : caught.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <Card title="URL do webhook" className={styles.card}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <Input
          label="URL"
          value={url}
          onChange={(event) => setUrl(event.target.value)}
          hint="Precisa usar https e apontar para um endereço público. Só o receptor de teste fica de fora dessa regra."
          placeholder="https://sualoja.example/webhooks"
          inputMode="url"
          autoComplete="off"
          spellCheck={false}
          required
        />
        <p role="status" className={styles.status}>
          {registrationStatus(registeredUrl, url)}
        </p>
        {error && (
          <p role="alert" className={styles.error}>
            {error}
          </p>
        )}
        <div className={styles.actions}>
          <Button type="submit" disabled={pending}>
            Salvar
          </Button>
          <Button variant="secondary" onClick={fillWithTestReceiver} disabled={pending}>
            Usar o receptor de teste
          </Button>
        </div>
      </form>
    </Card>
  )
}

export default WebhookEndpointForm
