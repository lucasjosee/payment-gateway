import { useEffect, useState } from 'react'
import Button from '../components/Button.jsx'
import { useSession } from '../session/useSession.js'
import ConsoleResizeHandle from './ConsoleResizeHandle.jsx'
import ConsoleTable from './ConsoleTable.jsx'
import ServerLogList from './ServerLogList.jsx'
import ServerLogsToolbar from './ServerLogsToolbar.jsx'
import { loadPreferences, savePreferences } from './consolePreferences.js'
import { isError } from './entryStatus.js'
import { useConsole } from './useConsole.js'
import { useServerLogs } from './useServerLogs.js'
import styles from './ConsolePanel.module.css'

function serverLogsEmptyMessage({ signedIn, status, detail, logFilter }) {
  if (!signedIn) {
    return 'Entre como lojista para ver os logs do servidor.'
  }
  if (status === 'unavailable') {
    return detail
  }
  if (status !== 'live') {
    return 'Conectando ao servidor…'
  }
  if (logFilter) {
    return 'Nenhuma linha deste pedido. Se ele é antigo, as linhas dele podem já ter saído do histórico do servidor.'
  }
  return 'Nenhum log ainda. As linhas dos seus pedidos aparecem aqui assim que acontecem.'
}

function ConsolePanel() {
  const { entries, highlightedTag, tab, logFilter, clear, selectTab, showServerLogs, clearLogFilter } = useConsole()
  const { merchant } = useSession()
  const [preferences, setPreferences] = useState(loadPreferences)
  const { open, height, filter } = preferences

  const showingServer = tab === 'server'
  const serverLogs = useServerLogs(open && showingServer && merchant !== null, merchant?.id)

  useEffect(() => savePreferences(preferences), [preferences])

  function update(changes) {
    setPreferences((current) => ({ ...current, ...changes }))
  }

  const errors = entries.filter(isError)
  const showingErrors = filter === 'errors'
  const logLines = logFilter ? serverLogs.lines.filter((line) => line.requestId === logFilter) : serverLogs.lines

  return (
    <section className={styles.panel} aria-label="Console de requisições">
      {open && <ConsoleResizeHandle height={height} onResize={(value) => update({ height: value })} />}

      <div className={styles.toolbar}>
        <Button variant="ghost" size="sm" aria-expanded={open} onClick={() => update({ open: !open })}>
          <svg className={open ? styles.chevronOpen : styles.chevron} viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 15l6-6 6 6" />
          </svg>
          Console
          <span className={styles.count}>{entries.length}</span>
        </Button>

        {open && (
          <div className={styles.tabs} role="group" aria-label="O que mostrar">
            <Button variant="ghost" size="sm" aria-pressed={!showingServer} onClick={() => selectTab('requests')}>
              Requisições
            </Button>
            <Button variant="ghost" size="sm" aria-pressed={showingServer} onClick={() => selectTab('server')}>
              Servidor
            </Button>
          </div>
        )}

        {open && showingServer && (
          <div className={styles.actions}>
            <ServerLogsToolbar status={serverLogs.status} logFilter={logFilter} onClearFilter={clearLogFilter} />
          </div>
        )}

        {open && !showingServer && (
          <div className={styles.actions}>
            <div className={styles.filters} role="group" aria-label="Filtrar requisições">
              <Button variant="ghost" size="sm" aria-pressed={!showingErrors} onClick={() => update({ filter: 'all' })}>
                Todas
              </Button>
              <Button variant="ghost" size="sm" aria-pressed={showingErrors} onClick={() => update({ filter: 'errors' })}>
                Só erros ({errors.length})
              </Button>
            </div>
            <Button variant="ghost" size="sm" onClick={clear} disabled={entries.length === 0}>
              Limpar
            </Button>
          </div>
        )}
      </div>

      {open && showingServer && (
        <ServerLogList
          lines={logLines}
          height={height}
          onFilter={showServerLogs}
          emptyMessage={serverLogsEmptyMessage({ signedIn: merchant !== null, ...serverLogs, logFilter })}
        />
      )}

      {open && !showingServer && (
        <ConsoleTable
          entries={showingErrors ? errors : entries}
          highlightedTag={highlightedTag}
          height={height}
          emptyMessage={
            showingErrors
              ? 'Nenhum erro até agora.'
              : 'Nenhuma requisição ainda. Tudo o que a página pedir à API aparece aqui, na hora.'
          }
        />
      )}
    </section>
  )
}

export default ConsolePanel
