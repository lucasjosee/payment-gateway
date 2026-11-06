import { encode } from 'uqr'
import styles from './PixQrCode.module.css'

// A especificação do QR code pede uma margem de 4 módulos, sem a qual alguns leitores não acham o código.
const QUIET_ZONE_MODULES = 4

function darkModulesPath(matrix) {
  return matrix
    .flatMap((row, y) => row.map((isDark, x) => (isDark ? `M${x} ${y}h1v1h-1z` : '')))
    .join('')
}

function PixQrCode({ text }) {
  const { size, data } = encode(text, { border: QUIET_ZONE_MODULES })

  return (
    <svg
      className={styles.qrCode}
      viewBox={`0 0 ${size} ${size}`}
      shapeRendering="crispEdges"
      role="img"
      aria-label="QR code do link de pagamento"
    >
      <path d={darkModulesPath(data)} />
    </svg>
  )
}

export default PixQrCode
