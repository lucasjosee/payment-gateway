import { useEffect, useState } from 'react'
import Button from '../components/Button.jsx'
import ConsoleResizeHandle from './ConsoleResizeHandle.jsx'
import ConsoleTable from './ConsoleTable.jsx'
import { loadPreferences, savePreferences } from './consolePreferences.js'
import { isError } from './entryStatus.js'
import { useConsole } from './useConsole.js'
import styles from './ConsolePanel.module.css'

function ConsolePanel() {
  const { entries, highlightedTag, clear } = useConsole()
  const [preferences, setPreferences] = useState(loadPreferences)
  const { open, height, filter } = preferences

  useEffect(() => savePreferences(preferences), [preferences])

  function update(changes) {
    setPreferences((current) => ({ ...current, ...changes }))
  }

  const errors = entries.filter(isError)
  const showingErrors = filter === 'errors'

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

      {open && (
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
