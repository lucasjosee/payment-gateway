import CopyButton from '../components/CopyButton.jsx'
import ConsoleMessage from './ConsoleMessage.jsx'
import { toCurl } from './toCurl.js'
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
  return (
    <div className={styles.detail}>
      <div className={styles.toolbar}>
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
