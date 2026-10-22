import styles from './ConsoleMessage.module.css'

function formatBody(body) {
  return typeof body === 'string' ? body : JSON.stringify(body, null, 2)
}

// Um dos lados da requisição, o envio ou a resposta. Enquanto esse lado ainda não existe, como uma resposta que não
// chegou, o placeholder aparece no lugar dos headers e do corpo.
function ConsoleMessage({ title, headers, body, placeholder }) {
  if (placeholder) {
    return (
      <section className={styles.message}>
        <h3 className={styles.title}>{title}</h3>
        <p className={styles.muted}>{placeholder}</p>
      </section>
    )
  }

  const headerEntries = Object.entries(headers)
  return (
    <section className={styles.message}>
      <h3 className={styles.title}>{title}</h3>

      {headerEntries.length === 0 ? (
        <p className={styles.muted}>Sem headers.</p>
      ) : (
        <dl className={styles.headers}>
          {headerEntries.map(([name, value]) => (
            <div key={name} className={styles.header}>
              <dt className={styles.muted}>{name}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      )}

      {body === null ? <p className={styles.muted}>Sem corpo.</p> : <pre className={styles.body}>{formatBody(body)}</pre>}
    </section>
  )
}

export default ConsoleMessage
