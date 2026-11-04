import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

const PAGE_SIZE = 10

// Busca uma página de GET /webhook-deliveries, no mesmo modelo do useChargePage.
export function useWebhookDeliveryPage(page) {
  const [reloadCount, setReloadCount] = useState(0)
  const [response, setResponse] = useState({ requestKey: null, data: null, error: null })
  const requestKey = `${page}:${reloadCount}`

  useEffect(() => {
    let active = true
    api
      .get(`/webhook-deliveries?page=${page}&size=${PAGE_SIZE}`)
      .then((data) => {
        if (active) {
          setResponse({ requestKey, data, error: null })
        }
      })
      .catch((error) => {
        if (active) {
          setResponse({ requestKey, data: null, error })
        }
      })
    return () => {
      active = false
    }
  }, [page, requestKey])

  return {
    data: response.data,
    error: response.error,
    loading: response.requestKey !== requestKey,
    reload: () => setReloadCount((count) => count + 1),
  }
}
