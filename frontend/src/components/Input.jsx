import { useId } from 'react'
import styles from './Input.module.css'

// A dica ou o erro ficam ligados ao campo pelo aria-describedby, então um leitor de tela os lê junto com o rótulo.
// Quando há erro, ele toma o lugar da dica.
function Input({ label, hint, error, id, className, ...props }) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const messageId = `${inputId}-message`
  const message = error ?? hint

  return (
    <div className={[styles.field, className].filter(Boolean).join(' ')}>
      <label className={styles.label} htmlFor={inputId}>
        {label}
      </label>
      <input
        id={inputId}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={message ? messageId : undefined}
        {...props}
      />
      {message && (
        <p id={messageId} className={error ? styles.error : styles.hint}>
          {message}
        </p>
      )}
    </div>
  )
}

export default Input
