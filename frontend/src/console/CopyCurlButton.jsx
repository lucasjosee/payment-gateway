import { useEffect, useState } from 'react'
import Button from '../components/Button.jsx'
import { toCurl } from './toCurl.js'

const FEEDBACK_DURATION_MS = 2000

const LABELS = {
  idle: 'Copiar como curl',
  copied: 'Copiado',
  failed: 'Não deu para copiar',
}

function CopyCurlButton({ entry }) {
  const [feedback, setFeedback] = useState('idle')

  // Depois de um tempo o rótulo volta ao normal. Se o botão sair da tela antes, o cronômetro é cancelado.
  useEffect(() => {
    if (feedback === 'idle') {
      return
    }
    const timer = setTimeout(() => setFeedback('idle'), FEEDBACK_DURATION_MS)
    return () => clearTimeout(timer)
  }, [feedback])

  // O navegador só libera a área de transferência em página segura (HTTPS ou localhost) e pode recusar o pedido.
  async function copy() {
    try {
      await navigator.clipboard.writeText(toCurl(entry, window.location.origin))
      setFeedback('copied')
    } catch {
      setFeedback('failed')
    }
  }

  return (
    <Button variant="secondary" size="sm" onClick={copy}>
      {LABELS[feedback]}
    </Button>
  )
}

export default CopyCurlButton
