import { useState } from 'react'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import CopyButton from '../components/CopyButton.jsx'
import styles from './SigningSecretCard.module.css'

const HIDDEN_SECRET = 'whsec_••••••••••••••••'

// Enquanto está escondido, o valor real nem entra na página: não dá para selecioná-lo nem achá-lo inspecionando o HTML.
function SigningSecretCard({ secret }) {
  const [visible, setVisible] = useState(false)

  return (
    <Card title="Segredo de assinatura" className={styles.card}>
      <p className={styles.description}>
        Com ele, o servidor da loja confere se um webhook veio mesmo da Ponte e chegou sem alterações. Guarde-o como uma
        senha. Ele continua o mesmo quando a URL muda.
      </p>
      <code className={styles.secret}>{visible ? secret : HIDDEN_SECRET}</code>
      <div className={styles.actions}>
        <Button variant="secondary" onClick={() => setVisible((current) => !current)}>
          {visible ? 'Esconder' : 'Mostrar'}
        </Button>
        <CopyButton variant="secondary" text={secret}>
          Copiar
        </CopyButton>
      </div>
    </Card>
  )
}

export default SigningSecretCard
