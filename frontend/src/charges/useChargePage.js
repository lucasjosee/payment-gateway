import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

const PAGE_SIZE = 10

// Busca uma página de GET /charges. Cada busca é identificada pela página e por quantas vezes a pessoa pediu para
// atualizar, e a lista está carregando enquanto a última resposta guardada não for a da busca atual. Assim o efeito
// só mexe no estado quando a resposta chega, e a página anterior continua na tela até a nova aparecer.
export function useChargePage(page) {
  const [reloadCount, setReloadCount] = useState(0)
  const [response, setResponse] = useState({ requestKey: null, data: null, error: null })
  const requestKey = `${page}:${reloadCount}`

  useEffect(() => {
    // Se a pessoa trocar de página antes de a resposta chegar, a resposta antiga é descartada.
    let active = true
    api
      .get(`/charges?page=${page}&size=${PAGE_SIZE}`)
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
