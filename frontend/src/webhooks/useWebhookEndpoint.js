import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

// Busca o webhook do lojista, no mesmo modelo do useCharge. Aqui o 404 não é erro: ele só quer dizer que o lojista
// ainda não cadastrou nenhum, e a página mostra o campo vazio.
export function useWebhookEndpoint() {
  const [reloadCount, setReloadCount] = useState(0)
  const [response, setResponse] = useState({ requestKey: null, endpoint: null, error: null })

  useEffect(() => {
    let active = true
    api
      .get('/webhook-endpoint')
      .then((endpoint) => {
        if (active) {
          setResponse({ requestKey: reloadCount, endpoint, error: null })
        }
      })
      .catch((error) => {
        if (active) {
          setResponse({ requestKey: reloadCount, endpoint: null, error: error.status === 404 ? null : error })
        }
      })
    return () => {
      active = false
    }
  }, [reloadCount])

  return {
    endpoint: response.endpoint,
    error: response.error,
    loading: response.requestKey !== reloadCount,
    update: (endpoint) => setResponse({ requestKey: reloadCount, endpoint, error: null }),
    reload: () => setReloadCount((count) => count + 1),
  }
}
