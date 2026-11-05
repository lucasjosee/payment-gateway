import { usePolling } from '../api/usePolling.js'

const PAGE_SIZE = 10

export function useWebhookDeliveryPage(page) {
  return usePolling(`/webhook-deliveries?page=${page}&size=${PAGE_SIZE}`)
}
