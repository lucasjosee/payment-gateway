import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

// Busca uma cobrança por GET /charges/{id}, no mesmo modelo do useChargePage: cada busca é identificada pelo id e
// por quantas vezes a pessoa pediu para tentar de novo, e está carregando enquanto a resposta guardada não for a da
// busca atual. O update troca a cobrança pela versão que uma ação devolveu, sem buscar de novo.
export function useCharge(id) {
  const [reloadCount, setReloadCount] = useState(0)
  const [response, setResponse] = useState({ requestKey: null, charge: null, error: null })
  const requestKey = `${id}:${reloadCount}`

  useEffect(() => {
    let active = true
    api
      .get(`/charges/${encodeURIComponent(id)}`)
      .then((charge) => {
        if (active) {
          setResponse({ requestKey, charge, error: null })
        }
      })
      .catch((error) => {
        if (active) {
          setResponse({ requestKey, charge: null, error })
        }
      })
    return () => {
      active = false
    }
  }, [id, requestKey])

  const current = response.requestKey === requestKey
  return {
    charge: current ? response.charge : null,
    error: current ? response.error : null,
    loading: !current,
    update: (charge) => setResponse({ requestKey, charge, error: null }),
    reload: () => setReloadCount((count) => count + 1),
  }
}
