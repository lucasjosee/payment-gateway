import Button from '../components/Button.jsx'
import CopyButton from '../components/CopyButton.jsx'
import ConsoleMessage from './ConsoleMessage.jsx'
import { toCurl } from './toCurl.js'
import { useConsole } from './useConsole.js'
import styles from './ConsoleDetail.module.css'

function responsePlaceholder(entry) {
  if (entry.error !== null) {
    return entry.error
  }
  if (entry.status === null) {
    return 'Aguardando a resposta.'
  }
  return null
}

function ConsoleDetail({ entry }) {
  const { showServerLogs } = useConsole()

  return (
    <div className={styles.detail}>
      <div className={styles.toolbar}>
        {entry.requestId && (
          <Button variant="ghost" size="sm" onClick={() => showServerLogs(entry.requestId)}>
            Ver logs do servidor
          </Button>
        )}
        <CopyButton variant="secondary" size="sm" text={toCurl(entry, window.location.origin)}>
          Copiar como curl
        </CopyButton>
      </div>
      <div className={styles.messages}>
        <ConsoleMessage title="Envio" headers={entry.requestHeaders} body={entry.requestBody} />
        <ConsoleMessage
          title="Resposta"
          headers={entry.responseHeaders}
          body={entry.responseBody}
          placeholder={responsePlaceholder(entry)}
        />
      </div>
    </div>
  )
}

export default ConsoleDetail
