import { useState } from 'react'
import { api } from '../api/client.js'
import { usePolling } from '../api/usePolling.js'

function isSettled(pix) {
  return pix.status !== 'PENDING'
}

// A consulta se repete porque, com a página aberta, o lojista pode cancelar o Pix ou o prazo pode vencer.
export function usePixPayment(id) {
  const path = `/dev/pix/${encodeURIComponent(id)}`
  const { data, error: loadError, reload } = usePolling(path, { until: isSettled })
  const [paidPix, setPaidPix] = useState(null)
  const [paying, setPaying] = useState(false)
  const [payError, setPayError] = useState(null)

  async function pay() {
    setPaying(true)
    setPayError(null)
    try {
      setPaidPix(await api.post(`${path}/pay`))
    } catch (error) {
      setPayError(error)
      // Um 409 quer dizer que o Pix mudou depois da última consulta, então a tela precisa buscar o estado atual.
      if (error.status === 409) {
        reload()
      }
    } finally {
      setPaying(false)
    }
  }

  return { pix: paidPix ?? data, loadError, payError, paying, pay, reload }
}
