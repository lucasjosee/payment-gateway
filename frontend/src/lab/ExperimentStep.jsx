import styles from './ExperimentStep.module.css'

// status: done, current ou waiting. Uma etapa que ainda não chegou mostra só o título, para o texto dela não adiantar
// o que vai acontecer.
function ExperimentStep({ number, title, status, children }) {
  return (
    <li className={`${styles.step} ${styles[status]}`} aria-current={status === 'current' ? 'step' : undefined}>
      <span className={styles.number} aria-hidden="true">
        {status === 'done' ? '✓' : number}
      </span>
      <div className={styles.content}>
        <h3 className={styles.title}>{title}</h3>
        {status !== 'waiting' && children}
      </div>
    </li>
  )
}

export default ExperimentStep
