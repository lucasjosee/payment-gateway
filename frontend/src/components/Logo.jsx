import styles from './Logo.module.css'

// O símbolo é uma ponte de pedra vista de lado: o tabuleiro em cima e o vão em arco embaixo. O mesmo desenho está
// em public/favicon.svg.
function Logo() {
  return (
    <span className={styles.logo}>
      <svg className={styles.mark} viewBox="0 0 32 32" aria-hidden="true">
        <rect className={styles.tile} width="32" height="32" rx="9" />
        <path className={styles.bridge} d="M6 11h20v12h-3a7 7 0 0 0-14 0H6z" />
      </svg>
      <span className={styles.wordmark}>Ponte</span>
    </span>
  )
}

export default Logo
