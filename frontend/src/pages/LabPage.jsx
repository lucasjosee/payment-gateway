import ConcurrencyExperiment from '../lab/ConcurrencyExperiment.jsx'
import IdempotencyExperiment from '../lab/IdempotencyExperiment.jsx'
import styles from './LabPage.module.css'

function LabPage() {
  return (
    <>
      <h1>Laboratório de garantias</h1>
      <p className={styles.intro}>
        Dois experimentos que põem à prova as garantias do gateway: repetir um pedido não cobra duas vezes, e pedidos
        simultâneos não capturam a mesma cobrança duas vezes. Tudo o que eles enviam aparece no console.
      </p>
      <div className={styles.experiments}>
        <IdempotencyExperiment />
        <ConcurrencyExperiment />
      </div>
    </>
  )
}

export default LabPage
