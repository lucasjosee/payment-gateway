import styles from './ButtonGroup.module.css'

function ButtonGroup({ legend, hint, children }) {
  return (
    <fieldset className={styles.group}>
      <legend className={styles.legend}>{legend}</legend>
      <div className={styles.buttons}>{children}</div>
      {hint && <p className={styles.hint}>{hint}</p>}
    </fieldset>
  )
}

export default ButtonGroup
