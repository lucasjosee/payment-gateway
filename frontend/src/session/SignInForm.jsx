import { useState } from 'react'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import { useSession } from './useSession.js'
import styles from './SessionForm.module.css'

// A mensagem de 401 da API fala em header, o que faz sentido para quem integra pelo terminal, não para quem está na tela.
const UNKNOWN_KEY_MESSAGE = 'Essa chave não foi reconhecida. Confira se você copiou a chave inteira.'

function SignInForm() {
  const { signIn } = useSession()
  const [enteredKey, setEnteredKey] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      // Uma chave colada costuma vir com um espaço ou uma quebra de linha nas pontas.
      await signIn(enteredKey.trim())
    } catch (caught) {
      setError(caught.status === 401 ? UNKNOWN_KEY_MESSAGE : caught.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title="Entrar com uma API key">
      <p className={styles.description}>Já criou um lojista? Cole a chave que você guardou.</p>
      <form className={styles.form} onSubmit={handleSubmit}>
        {/* Campo de senha para a chave não ficar à vista de quem olha a tela. */}
        <Input
          label="API key"
          type="password"
          value={enteredKey}
          onChange={(event) => setEnteredKey(event.target.value)}
          required
          autoComplete="off"
          spellCheck={false}
        />
        {error && (
          <p role="alert" className={styles.error}>
            {error}
          </p>
        )}
        <Button type="submit" className={styles.submit} disabled={submitting}>
          {submitting ? 'Entrando…' : 'Entrar'}
        </Button>
      </form>
    </Card>
  )
}

export default SignInForm
