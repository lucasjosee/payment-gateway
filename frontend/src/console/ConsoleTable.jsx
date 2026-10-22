import ConsoleRow from './ConsoleRow.jsx'
import { useFollowScroll } from './useFollowScroll.js'
import styles from './ConsoleTable.module.css'

function ConsoleTable({ entries, highlightedTag, height, emptyMessage }) {
  const { ref, onScroll } = useFollowScroll()

  return (
    <div ref={ref} className={styles.scroll} style={{ height }} onScroll={onScroll}>
      {entries.length === 0 ? (
        <p className={styles.empty}>{emptyMessage}</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th scope="col" className={styles.toggle} aria-label="Detalhes" />
              <th scope="col" className={styles.time}>Horário</th>
              <th scope="col" className={styles.method}>Método</th>
              <th scope="col">Rota</th>
              <th scope="col" className={styles.status}>Status</th>
              <th scope="col" className={styles.duration}>Duração</th>
              <th scope="col" className={styles.requestId}>Request ID</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <ConsoleRow
                key={entry.id}
                entry={entry}
                highlighted={entry.tag !== null && entry.tag === highlightedTag}
              />
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default ConsoleTable
