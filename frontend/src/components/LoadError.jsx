import Button from './Button.jsx'
import styles from './LoadError.module.css'

// O que a página mostra quando a busca dos dados dela falha. O request ID liga a mensagem à linha do console e às
// linhas de log do servidor daquele pedido.
function LoadError({ message, error, onRetry }) {
  return (
    <div role="alert" className={styles.error}>
      <p>
        {message} {error.message}
      </p>
      {error.requestId && <p className={styles.requestId}>Request ID {error.requestId}</p>}
      <Button variant="secondary" size="sm" onClick={onRetry}>
        Tentar de novo
      </Button>
    </div>
  )
}

export default LoadError
