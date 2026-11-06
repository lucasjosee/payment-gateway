import { useState } from 'react'
import Badge from '../components/Badge.jsx'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import { formatCents } from './money.js'
import PixCountdown from './PixCountdown.jsx'
import styles from './PixPayment.module.css'

const OUTCOMES = {
  PAID: { label: 'Pago', tone: 'success', text: 'Pagamento confirmado. O lojista recebe o aviso em instantes.' },
  EXPIRED: { label: 'Vencido', tone: 'warning', text: 'O prazo acabou antes do pagamento. Peça um novo Pix ao lojista.' },
  CANCELED: { label: 'Cancelado', tone: 'neutral', text: 'O lojista cancelou este Pix, e ele não pode mais ser pago.' },
  REFUNDED: { label: 'Estornado', tone: 'neutral', text: 'O pagamento foi feito e depois devolvido pelo lojista.' },
}

function PixPayment({ pix, paying, error, onPay }) {
  const [deadlinePassed, setDeadlinePassed] = useState(false)
  const amount = formatCents(pix.amountCents, pix.currency)
  // Depois do prazo, a API ainda responde PENDING até o job de expiração passar, o que leva até 10 segundos.
  const outcome = OUTCOMES[pix.status === 'PENDING' && deadlinePassed ? 'EXPIRED' : pix.status]

  return (
    <Card>
      <div>
        <h1>{amount}</h1>
        <p className={styles.merchant}>para {pix.merchantName}</p>
      </div>
      <div className={styles.action}>
        {outcome ? (
          <div role="status" className={styles.outcome}>
            <Badge tone={outcome.tone}>{outcome.label}</Badge>
            <p className={styles.muted}>{outcome.text}</p>
          </div>
        ) : (
          <>
            <PixCountdown expiresAt={pix.expiresAt} onExpire={() => setDeadlinePassed(true)} />
            <Button className={styles.pay} onClick={onPay} disabled={paying}>
              {paying ? 'Pagando…' : `Pagar ${amount}`}
            </Button>
            {error && (
              <p role="alert" className={styles.error}>
                {error.message}
              </p>
            )}
          </>
        )}
      </div>
    </Card>
  )
}

export default PixPayment
