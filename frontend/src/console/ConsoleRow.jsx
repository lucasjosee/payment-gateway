import { useEffect, useRef, useState } from 'react'
import ConsoleDetail from './ConsoleDetail.jsx'
import { statusLabel, statusTone } from './entryStatus.js'
import { formatTime } from './formatTime.js'
import styles from './ConsoleRow.module.css'

function ConsoleRow({ entry, highlighted }) {
  const [expanded, setExpanded] = useState(false)
  const detailRef = useRef(null)

  // A linha aberta costuma ser a última, e o detalhe nasceria abaixo da área visível do console. Rolar até ele mostra
  // o que se abriu sem a pessoa precisar procurar.
  useEffect(() => {
    if (expanded) {
      detailRef.current.scrollIntoView({ block: 'nearest' })
    }
  }, [expanded])

  return (
    <>
      {/* Com o mouse, a linha inteira abre o detalhe. O botão é o caminho pelo teclado: o clique dele sobe até a
          linha, então os dois passam pelo mesmo onClick. */}
      <tr
        className={[styles.row, expanded && styles.expanded, highlighted && styles.highlighted].filter(Boolean).join(' ')}
        onClick={() => setExpanded(!expanded)}
      >
        <td>
          <button type="button" className={styles.toggle} aria-expanded={expanded} aria-label="Detalhes da requisição">
            <svg className={styles.chevron} viewBox="0 0 24 24" aria-hidden="true">
              <path d="M9 6l6 6-6 6" />
            </svg>
          </button>
        </td>
        <td className={styles.muted}>{formatTime(entry.startedAt)}</td>
        <td className={styles.method}>{entry.method}</td>
        <td title={entry.path}>{entry.path}</td>
        <td className={styles[statusTone(entry)]}>{statusLabel(entry)}</td>
        <td className={styles.duration}>{entry.durationMs === null ? '…' : `${entry.durationMs} ms`}</td>
        {/* Os 8 primeiros caracteres bastam para reconhecer o pedido. O ID inteiro aparece ao passar o mouse e nos
            headers da resposta, no detalhe. */}
        <td className={styles.muted} title={entry.requestId ?? undefined}>
          {entry.requestId ? entry.requestId.slice(0, 8) : '—'}
        </td>
      </tr>

      {expanded && (
        <tr>
          <td ref={detailRef} colSpan={7} className={styles.detail}>
            <ConsoleDetail entry={entry} />
          </td>
        </tr>
      )}
    </>
  )
}

export default ConsoleRow
