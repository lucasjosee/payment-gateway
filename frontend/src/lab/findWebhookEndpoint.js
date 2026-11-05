import { api } from '../api/client.js'

// Sem webhook cadastrado, a API responde 404, e aqui isso não é um erro.
export function findWebhookEndpoint(tag) {
  return api.get('/webhook-endpoint', { tag }).catch((error) => {
    if (error.status === 404) {
      return null
    }
    throw error
  })
}
