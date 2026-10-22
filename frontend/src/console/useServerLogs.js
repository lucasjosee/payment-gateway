import { useEffect, useState } from 'react'
import { ApiError, streamServerLogs } from '../api/client.js'

const MAX_LINES = 500
const RETRY_DELAY_MS = 3000
const DISCONNECTED = { status: 'connecting', detail: null, lines: [] }

const UNAVAILABLE = {
  401: 'A API não aceitou a chave desta sessão. Saia e entre de novo.',
  404: 'Esta API não tem o stream de logs. Ele só existe quando ela roda com o profile demo.',
}

// A cada conexão o servidor reenvia as linhas recentes, então a lista recomeça do zero em vez de repetir linhas.
export function useServerLogs(enabled, merchantId) {
  const [log, setLog] = useState(DISCONNECTED)

  useEffect(() => {
    if (!enabled) {
      return
    }
    const controller = new AbortController()
    let retryTimer = null

    function retryLater() {
      setLog((current) => ({ ...current, status: 'reconnecting' }))
      retryTimer = setTimeout(connect, RETRY_DELAY_MS)
    }

    function connect() {
      streamServerLogs({
        signal: controller.signal,
        onOpen: () => setLog({ status: 'live', detail: null, lines: [] }),
        onLine: (line) => setLog((current) => ({ ...current, lines: [...current.lines, line].slice(-MAX_LINES) })),
      })
        .then(() => {
          if (!controller.signal.aborted) {
            retryLater()
          }
        })
        .catch((error) => {
          if (controller.signal.aborted) {
            return
          }
          if (error instanceof ApiError && UNAVAILABLE[error.status]) {
            setLog({ status: 'unavailable', detail: UNAVAILABLE[error.status], lines: [] })
            return
          }
          retryLater()
        })
    }

    connect()
    return () => {
      controller.abort()
      clearTimeout(retryTimer)
      setLog(DISCONNECTED)
    }
  }, [enabled, merchantId])

  return log
}
