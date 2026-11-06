import { useState } from 'react'
import { api } from '../api/client.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import CardFields from './CardFields.jsx'
import IdempotencyKeyField from './IdempotencyKeyField.jsx'
import PaymentMethodPicker from './PaymentMethodPicker.jsx'
import TestCardButtons from './TestCardButtons.jsx'
import { parseReaisToCents } from './money.js'
import { testCardFields } from './testCards.js'
import styles from './ChargeForm.module.css'

const EMPTY_CARD = { number: '', expiry: '', cvv: '' }

function amountErrorFor(cents) {
  if (cents === null) {
    return 'Use só números, com vírgula antes dos centavos, como 1.500,00.'
  }
  if (cents === 0) {
    return 'Informe um valor maior que zero.'
  }
  return null
}

// Com o prazo em branco, ele fica de fora do pedido e a API aplica o padrão dela.
function paymentFields(method, card, deadlineSeconds) {
  if (method === 'pix') {
    return { pix: deadlineSeconds ? { expiresInSeconds: Number(deadlineSeconds) } : {} }
  }
  return { card: { ...card, number: card.number.replace(/\s/g, '') } }
}

function ChargeForm({ onCharged }) {
  const [method, setMethod] = useState('card')
  const [amount, setAmount] = useState('')
  const [amountError, setAmountError] = useState(null)
  const [currency, setCurrency] = useState('BRL')
  const [card, setCard] = useState(EMPTY_CARD)
  const [deadlineSeconds, setDeadlineSeconds] = useState('')
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID())
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  function pickMethod(next) {
    setMethod(next)
    setError(null)
    // O cartão de teste "Moeda USD" pode ter deixado outra moeda no campo, e o Pix só aceita BRL.
    if (next === 'pix') {
      setCurrency('BRL')
    }
  }

  function updateCard(changes) {
    setCard((current) => ({ ...current, ...changes }))
  }

  function pickTestCard(testCard) {
    const fields = testCardFields(testCard)
    setCurrency(fields.currency)
    setCard(fields.card)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const amountCents = parseReaisToCents(amount)
    const invalidAmount = amountErrorFor(amountCents)
    setAmountError(invalidAmount)
    setError(null)
    if (invalidAmount) {
      return
    }

    setSubmitting(true)
    try {
      const charge = await api.post('/charges', {
        body: { amountCents, currency, ...paymentFields(method, card, deadlineSeconds) },
        idempotencyKey,
      })
      onCharged(charge)
      // A chave usada ficou presa a esta cobrança. A próxima precisa de outra.
      setIdempotencyKey(crypto.randomUUID())
    } catch (caught) {
      // Sem cobrança na resposta, a chave fica. Se o pedido chegou a ser processado e só a resposta se perdeu,
      // reenviar com ela devolve a mesma cobrança em vez de criar outra.
      setError(caught)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title="Dados da cobrança">
      <form className={styles.form} onSubmit={handleSubmit}>
        <PaymentMethodPicker value={method} onChange={pickMethod} />
        {method === 'card' && <TestCardButtons onPick={pickTestCard} />}

        <div className={styles.amountRow}>
          <Input
            label="Valor"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
            error={amountError}
            hint="Como 1.500,00. A API recebe o valor em centavos."
            placeholder="150,00"
            inputMode="decimal"
            maxLength={18}
            required
          />
          <Input
            label="Moeda"
            value={currency}
            onChange={(event) => setCurrency(event.target.value.toUpperCase())}
            pattern="[A-Z]{3}"
            maxLength={3}
            required
          />
        </div>

        {method === 'card' ? (
          <CardFields card={card} onChange={updateCard} />
        ) : (
          <Input
            label="Prazo em segundos"
            value={deadlineSeconds}
            onChange={(event) => setDeadlineSeconds(event.target.value)}
            hint="Em branco, a API usa o padrão de 15 minutos. O máximo é 86400, que dá um dia."
            placeholder="900"
            inputMode="numeric"
            pattern="[0-9]+"
            maxLength={6}
            autoComplete="off"
          />
        )}

        <IdempotencyKeyField value={idempotencyKey} onChange={setIdempotencyKey} />

        {error && (
          <div role="alert" className={styles.error}>
            <p>{error.message}</p>
            {error.problem?.errors && (
              <ul>
                {error.problem.errors.map(({ field, message }) => (
                  <li key={`${field}:${message}`}>
                    <code>{field}</code>: {message}
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        <Button type="submit" className={styles.submit} disabled={submitting}>
          {submitting ? 'Criando…' : 'Criar cobrança'}
        </Button>
      </form>
    </Card>
  )
}

export default ChargeForm
