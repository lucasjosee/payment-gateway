import { useId } from 'react'
import Button from '../components/Button.jsx'
import Input from '../components/Input.jsx'
import styles from './IdempotencyKeyField.module.css'

// A explicação fica fora do Input para o botão se alinhar com a caixa do campo, e não com o texto embaixo dela. O
// aria-describedby refaz a ligação que o Input faria sozinho com a dica.
function IdempotencyKeyField({ value, onChange }) {
  const hintId = useId()

  return (
    <div className={styles.field}>
      <div className={styles.row}>
        <Input
          label="Idempotency-Key"
          className={styles.input}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          required
          maxLength={255}
          spellCheck={false}
          autoComplete="off"
          aria-describedby={hintId}
        />
        <Button variant="secondary" onClick={() => onChange(crypto.randomUUID())}>
          Gerar nova
        </Button>
      </div>
      <p id={hintId} className={styles.hint}>
        Reenviar com a mesma chave devolve a resposta da primeira vez, sem cobrar de novo. Depois de cada cobrança
        criada, o campo recebe uma chave nova.
      </p>
    </div>
  )
}

export default IdempotencyKeyField
