import styles from './Switch.module.css'

// Por baixo é um checkbox com role="switch", então o teclado e os leitores de tela funcionam sem código a mais. Para
// bloquear uma troca em andamento, use aria-disabled: o disabled tiraria o foco de quem está usando o teclado.
function Switch({ label, ...props }) {
  return (
    <label className={styles.switch}>
      <input type="checkbox" role="switch" className={styles.input} {...props} />
      <span className={styles.track} aria-hidden="true" />
      {label}
    </label>
  )
}

export default Switch
