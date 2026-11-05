import ConcurrencyExperiment from '../lab/ConcurrencyExperiment.jsx'
import GuaranteedDeliveryExperiment from '../lab/GuaranteedDeliveryExperiment.jsx'
import IdempotencyExperiment from '../lab/IdempotencyExperiment.jsx'
import styles from './LabPage.module.css'

function LabPage() {
  return (
    <>
      <h1>Laboratório de garantias</h1>
      <p className={styles.intro}>
        Três experimentos que põem à prova as garantias do gateway: repetir um pedido não cobra duas vezes, pedidos
        simultâneos não capturam a mesma cobrança duas vezes, e um webhook não se perde quando o servidor da loja está
        fora do ar. Tudo o que eles enviam aparece no console.
      </p>
      <div className={styles.experiments}>
        <IdempotencyExperiment />
        <ConcurrencyExperiment />
        <GuaranteedDeliveryExperiment />
      </div>
    </>
  )
}

export default LabPage
