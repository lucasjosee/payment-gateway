import { useEffect, useState } from 'react'
import { api } from '../api/client.js'
import Badge from './Badge.jsx'

const STATUSES = {
  checking: { tone: 'neutral', label: 'Verificando a API' },
  up: { tone: 'success', label: 'API no ar' },
  down: { tone: 'danger', label: 'API fora do ar' },
}

// Confere a API uma vez, ao abrir a página. Não repete sozinho porque cada verificação seria uma linha a mais no
// console, misturada às requisições que a pessoa está fazendo.
function ApiStatus() {
  const [status, setStatus] = useState('checking')

  useEffect(() => {
    // Se o componente sair da tela antes de a resposta chegar, o resultado é descartado.
    let active = true
    api
      .get('/actuator/health')
      .then((health) => {
        if (active) {
          setStatus(health.status === 'UP' ? 'up' : 'down')
        }
      })
      .catch(() => {
        if (active) {
          setStatus('down')
        }
      })
    return () => {
      active = false
    }
  }, [])

  const { tone, label } = STATUSES[status]
  return <Badge tone={tone}>{label}</Badge>
}

export default ApiStatus
