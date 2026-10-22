import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

// Busca uma cobrança por GET /charges/{id}, no mesmo modelo do useChargePage: está carregando enquanto a resposta
// guardada não for a do id atual. O update troca a cobrança pela versão que uma ação devolveu, sem buscar de novo.
export function useCharge(id) {
  const [response, setResponse] = useState({ id: null, charge: null, error: null })

  useEffect(() => {
    let active = true
    api
      .get(`/charges/${encodeURIComponent(id)}`)
      .then((charge) => {
        if (active) {
          setResponse({ id, charge, error: null })
        }
      })
      .catch((error) => {
        if (active) {
          setResponse({ id, charge: null, error })
        }
      })
    return () => {
      active = false
    }
  }, [id])

  const current = response.id === id
  return {
    charge: current ? response.charge : null,
    error: current ? response.error : null,
    loading: !current,
    update: (charge) => setResponse({ id, charge, error: null }),
  }
}
