import { formatTime } from './formatTime.js'
import { useFollowScroll } from './useFollowScroll.js'
import styles from './ServerLogList.module.css'

function shortLogger(logger) {
  return logger.split('.').at(-1)
}

function ServerLogList({ lines, height, emptyMessage, onFilter }) {
  const { ref, onScroll } = useFollowScroll()

  return (
    <div ref={ref} className={styles.scroll} style={{ height }} onScroll={onScroll}>
      {lines.length === 0 ? (
        <p className={styles.empty}>{emptyMessage}</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th scope="col" className={styles.time}>Horário</th>
              <th scope="col" className={styles.level}>Nível</th>
              <th scope="col" className={styles.requestId}>Request ID</th>
              <th scope="col" className={styles.logger}>Origem</th>
              <th scope="col">Mensagem</th>
            </tr>
          </thead>
          <tbody>
            {lines.map((line, index) => (
              <tr key={index} className={styles.line}>
                <td className={styles.muted}>{formatTime(line.timestamp)}</td>
                <td className={styles[line.level.toLowerCase()]}>{line.level}</td>
                <td>
                  {line.requestId && (
                    <button
                      type="button"
                      className={styles.requestIdButton}
                      title={`Mostrar só as linhas do pedido ${line.requestId}`}
                      onClick={() => onFilter(line.requestId)}
                    >
                      {line.requestId.slice(0, 8)}
                    </button>
                  )}
                </td>
                <td className={styles.muted} title={line.logger}>
                  {shortLogger(line.logger)}
                </td>
                <td className={styles.message}>{line.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default ServerLogList
