import { useLayoutEffect, useRef } from 'react'
import ConsoleRow from './ConsoleRow.jsx'
import styles from './ConsoleTable.module.css'

// Até esta distância do fim, em pixels, a lista ainda conta como "no fim".
const FOLLOW_THRESHOLD = 32

function ConsoleTable({ entries, height, emptyMessage }) {
  const scrollRef = useRef(null)
  const followingRef = useRef(true)

  // Se a lista está no fim, ela acompanha as requisições novas. Se a pessoa rolou para cima para ler alguma coisa,
  // a lista fica onde está. O useLayoutEffect rola antes de o navegador pintar, então a linha nova nunca aparece
  // escondida por um instante.
  useLayoutEffect(() => {
    if (followingRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [entries, height])

  function handleScroll() {
    const list = scrollRef.current
    followingRef.current = list.scrollHeight - list.scrollTop - list.clientHeight < FOLLOW_THRESHOLD
  }

  return (
    <div ref={scrollRef} className={styles.scroll} style={{ height }} onScroll={handleScroll}>
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
              <ConsoleRow key={entry.id} entry={entry} />
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default ConsoleTable
