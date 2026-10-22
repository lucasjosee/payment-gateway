import { useState } from 'react'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import styles from './SessionForm.module.css'

// Só cadastra o lojista. A sessão começa depois, quando a pessoa confirma que guardou a chave.
function CreateMerchantForm({ onCreated }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      onCreated(await api.post('/merchants', { body: { name: name.trim(), email: email.trim() } }))
    } catch (caught) {
      setError(caught.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title="Criar lojista de teste">
      <p className={styles.description}>Cadastre uma loja para fazer cobranças. A API key aparece logo em seguida.</p>
      <form className={styles.form} onSubmit={handleSubmit}>
        <Input
          label="Nome da loja"
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
          maxLength={200}
          autoComplete="organization"
        />
        <Input
          label="E-mail"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
          maxLength={254}
          autoComplete="email"
        />
        {error && (
          <p role="alert" className={styles.error}>
            {error}
          </p>
        )}
        <Button type="submit" className={styles.submit} disabled={submitting}>
          {submitting ? 'Criando…' : 'Criar lojista'}
        </Button>
      </form>
    </Card>
  )
}

export default CreateMerchantForm
