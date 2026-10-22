import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import CopyButton from '../components/CopyButton.jsx'
import styles from './ApiKeyReveal.module.css'

// A única tela que mostra a chave inteira. Ela vem da resposta do cadastro e não fica guardada em lugar nenhum além
// do estado da página de boas-vindas, até a pessoa continuar.
function ApiKeyReveal({ merchantName, apiKey, onContinue }) {
  return (
    <Card title="Lojista criado" className={styles.reveal}>
      <p>{merchantName} já pode fazer cobranças com a API key abaixo.</p>
      <p className={styles.warning}>
        Esta é a única vez que a chave aparece. O gateway guarda só um hash dela e não tem como mostrá-la de novo,
        então copie e guarde antes de continuar.
      </p>
      <code className={styles.key}>{apiKey}</code>
      <div className={styles.actions}>
        <CopyButton variant="secondary" text={apiKey}>
          Copiar chave
        </CopyButton>
        <Button onClick={onContinue}>Já guardei, continuar</Button>
      </div>
    </Card>
  )
}

export default ApiKeyReveal
