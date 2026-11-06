import { useEffect, useState } from 'react'
import styles from './PixCountdown.module.css'

function formatTimeLeft(totalSeconds) {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const parts = hours > 0 ? [hours, minutes, seconds] : [minutes, seconds]
  return parts.map((part) => String(part).padStart(2, '0')).join(':')
}

// O tempo restante sai sempre da conta com o prazo, e não de um desconto a cada segundo. O navegador atrasa os
// cronômetros de uma aba escondida, e um desconto ficaria para trás junto com eles.
function PixCountdown({ expiresAt, onExpire }) {
  const [now, setNow] = useState(() => Date.now())
  const msLeft = Date.parse(expiresAt) - now
  const expired = msLeft <= 0

  useEffect(() => {
    if (expired) {
      onExpire?.()
    }
  }, [expired, onExpire])

  // Acorda na virada do próximo segundo, em vez de a cada 1000 ms, para o "Vencido" aparecer no horário exato do prazo.
  useEffect(() => {
    if (expired) {
      return
    }
    const timer = setTimeout(() => setNow(Date.now()), msLeft % 1000 || 1000)
    return () => clearTimeout(timer)
  }, [expired, msLeft])

  if (expired) {
    return (
      <p role="timer" className={styles.expired}>
        Vencido
      </p>
    )
  }
  return (
    <p role="timer" className={styles.countdown}>
      Vence em <strong className={styles.time}>{formatTimeLeft(Math.ceil(msLeft / 1000))}</strong>
    </p>
  )
}

export default PixCountdown
