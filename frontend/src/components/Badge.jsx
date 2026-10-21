import styles from './Badge.module.css'

// tone: neutral, info, success, warning ou danger.
function Badge({ tone = 'neutral', children }) {
  return <span className={`${styles.badge} ${styles[tone]}`}>{children}</span>
}

export default Badge
