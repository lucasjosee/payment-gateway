import { useState } from 'react'
import { Navigate } from 'react-router'
import ApiKeyReveal from '../session/ApiKeyReveal.jsx'
import CreateMerchantForm from '../session/CreateMerchantForm.jsx'
import SignInForm from '../session/SignInForm.jsx'
import { useSession } from '../session/useSession.js'
import styles from './WelcomePage.module.css'

function WelcomePage() {
  const { merchant, startSession } = useSession()
  const [created, setCreated] = useState(null)

  // Quem já entrou não tem o que fazer aqui. Isso vale também logo depois de entrar ou de confirmar a chave nova:
  // a sessão começa, esta página renderiza de novo e leva a pessoa para as cobranças.
  if (merchant) {
    return <Navigate to="/cobrancas" replace />
  }

  if (created) {
    const { apiKey, ...newMerchant } = created
    return (
      <ApiKeyReveal
        merchantName={newMerchant.name}
        apiKey={apiKey}
        onContinue={() => startSession(apiKey, newMerchant)}
      />
    )
  }

  return (
    <>
      <h1>Boas-vindas</h1>
      <p className={styles.intro}>
        Ponte é um gateway de pagamento de estudo. Para usar, crie um lojista de teste ou entre com a API key de um que
        você já criou.
      </p>
      <div className={styles.options}>
        <CreateMerchantForm onCreated={setCreated} />
        <SignInForm />
      </div>
    </>
  )
}

export default WelcomePage
