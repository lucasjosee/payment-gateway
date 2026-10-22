import { useEffect, useState } from 'react'
import Button from './Button.jsx'

const FEEDBACK_DURATION_MS = 2000

const FEEDBACK_LABELS = {
  copied: 'Copiado',
  failed: 'Não deu para copiar',
}

// Copia o text para a área de transferência. Por um instante, o rótulo confirma a cópia ou avisa que ela falhou.
function CopyButton({ text, children, ...props }) {
  const [feedback, setFeedback] = useState(null)

  // Depois de um tempo o rótulo volta ao normal. Se o botão sair da tela antes, o cronômetro é cancelado.
  useEffect(() => {
    if (!feedback) {
      return
    }
    const timer = setTimeout(() => setFeedback(null), FEEDBACK_DURATION_MS)
    return () => clearTimeout(timer)
  }, [feedback])

  // O navegador só libera a área de transferência em página segura (HTTPS ou localhost) e pode recusar o pedido.
  async function copy() {
    try {
      await navigator.clipboard.writeText(text)
      setFeedback('copied')
    } catch {
      setFeedback('failed')
    }
  }

  return (
    <Button {...props} onClick={copy}>
      {FEEDBACK_LABELS[feedback] ?? children}
    </Button>
  )
}

export default CopyButton
