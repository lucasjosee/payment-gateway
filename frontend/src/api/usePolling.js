import { useEffect, useState } from 'react'
import { api } from './client.js'

const INTERVAL_MS = 2000

// Repete um GET enquanto o componente está na tela. A primeira busca e as pedidas com reload são pedidos normais,
// porque foi a pessoa que os causou. As repetições vão marcadas como de segundo plano, para o console poder
// escondê-las. A próxima só é agendada depois que a anterior termina: com a API lenta, um intervalo fixo acumularia
// pedidos em paralelo, e uma resposta velha poderia chegar depois de uma nova.
//
// Com path nulo, ainda não há o que consultar. O until recebe cada resposta e, ao devolver true, encerra as repetições.
// Ele precisa manter a mesma referência entre os renders, como uma função declarada fora do componente.
export function usePolling(path, { until, tag } = {}) {
  const [reloadCount, setReloadCount] = useState(0)
  const [response, setResponse] = useState({ requestKey: null, data: null, error: null })
  const requestKey = `${path}:${reloadCount}`

  useEffect(() => {
    if (path === null) {
      return
    }
    let active = true
    let timer = null

    async function load(background) {
      try {
        const data = await api.get(path, { background, tag })
        if (active) {
          setResponse({ requestKey, data, error: null })
        }
        if (until?.(data)) {
          return
        }
      } catch (error) {
        // Repetir não faz aparecer uma rota que não existe, como a do receptor de teste fora do profile demo.
        const definitive = error.status === 404
        if (active) {
          // Uma repetição que falha não apaga o que já está na tela, porque a seguinte ainda pode dar certo.
          const keepData = background && !definitive
          setResponse((current) =>
            keepData ? { ...current, error: current.error ?? error } : { requestKey, data: null, error },
          )
        }
        if (definitive) {
          return
        }
      }
      if (active) {
        timer = setTimeout(repeat, INTERVAL_MS)
      }
    }

    // Com a aba escondida, ninguém está olhando: o ciclo continua, mas sem pedir nada à API.
    function repeat() {
      if (document.hidden) {
        timer = setTimeout(repeat, INTERVAL_MS)
      } else {
        load(true)
      }
    }

    load(false)
    return () => {
      active = false
      clearTimeout(timer)
    }
  }, [path, requestKey, until, tag])

  return {
    data: response.data,
    error: response.error,
    loading: response.requestKey !== requestKey,
    reload: () => setReloadCount((count) => count + 1),
  }
}
