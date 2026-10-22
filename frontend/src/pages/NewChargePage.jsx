import { useState } from 'react'
import ChargeForm from '../charges/ChargeForm.jsx'
import ChargeResult from '../charges/ChargeResult.jsx'
import Card from '../components/Card.jsx'
import styles from './NewChargePage.module.css'

function NewChargePage() {
  const [lastCharge, setLastCharge] = useState(null)

  return (
    <>
      <h1>Nova cobrança</h1>
      <p className={styles.intro}>
        Escolha um cartão de teste, informe o valor e crie a cobrança. O pedido e a resposta completos aparecem no
        console, logo abaixo.
      </p>
      <div className={styles.layout}>
        <ChargeForm onCharged={setLastCharge} />
        {lastCharge ? (
          <ChargeResult charge={lastCharge} />
        ) : (
          <Card title="Última cobrança">
            <p className={styles.muted}>A resposta da API aparece aqui depois que você criar uma cobrança.</p>
          </Card>
        )}
      </div>
    </>
  )
}

export default NewChargePage
