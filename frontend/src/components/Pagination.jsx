import Button from './Button.jsx'
import styles from './Pagination.module.css'

// page começa em 0, como na API. Na tela, a contagem começa em 1.
function Pagination({ page, totalPages, onChange, disabled = false }) {
  return (
    <nav className={styles.pagination} aria-label="Paginação">
      <Button variant="secondary" size="sm" onClick={() => onChange(page - 1)} disabled={disabled || page === 0}>
        Anterior
      </Button>
      <span className={styles.position}>
        Página {page + 1} de {Math.max(totalPages, 1)}
      </span>
      <Button
        variant="secondary"
        size="sm"
        onClick={() => onChange(page + 1)}
        disabled={disabled || page + 1 >= totalPages}
      >
        Próxima
      </Button>
    </nav>
  )
}

export default Pagination
