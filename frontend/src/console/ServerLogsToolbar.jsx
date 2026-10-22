import Button from '../components/Button.jsx'
import styles from './ServerLogsToolbar.module.css'

const STATUS_LABELS = {
  connecting: 'Conectando…',
  live: 'Ao vivo',
  reconnecting: 'Reconectando…',
  unavailable: 'Indisponível',
}

function ServerLogsToolbar({ status, logFilter, onClearFilter }) {
  return (
    <div className={styles.toolbar}>
      {logFilter && (
        <span className={styles.filter}>
          Pedido <code title={logFilter}>{logFilter.slice(0, 8)}</code>
          <Button variant="ghost" size="sm" onClick={onClearFilter}>
            Mostrar todos
          </Button>
        </span>
      )}
      <span className={`${styles.status} ${styles[status]}`}>{STATUS_LABELS[status]}</span>
    </div>
  )
}

export default ServerLogsToolbar
