import styles from './ConsoleResizeHandle.module.css'

const MIN_HEIGHT = 120
const KEYBOARD_STEP = 24

// O console nunca passa de 70% da janela, para a página não sumir por inteiro atrás dele.
function maxHeight() {
  return Math.round(window.innerHeight * 0.7)
}

function clamp(height) {
  return Math.min(Math.max(height, MIN_HEIGHT), maxHeight())
}

// A alça na borda de cima do console. Arrastar para cima aumenta a altura. Pelo teclado, as setas fazem o mesmo.
function ConsoleResizeHandle({ height, onResize }) {
  function handlePointerDown(event) {
    const handle = event.currentTarget
    const startY = event.clientY
    const startHeight = height

    // Com a captura, o movimento continua chegando à alça mesmo quando o ponteiro sai de cima dela durante o arrasto.
    handle.setPointerCapture(event.pointerId)

    function handlePointerMove(moveEvent) {
      onResize(clamp(startHeight + startY - moveEvent.clientY))
    }
    function stop() {
      handle.removeEventListener('pointermove', handlePointerMove)
      handle.removeEventListener('pointerup', stop)
      handle.removeEventListener('pointercancel', stop)
    }
    handle.addEventListener('pointermove', handlePointerMove)
    handle.addEventListener('pointerup', stop)
    handle.addEventListener('pointercancel', stop)
  }

  function handleKeyDown(event) {
    if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
      event.preventDefault()
      onResize(clamp(height + (event.key === 'ArrowUp' ? KEYBOARD_STEP : -KEYBOARD_STEP)))
    }
  }

  return (
    <div
      role="separator"
      aria-orientation="horizontal"
      aria-label="Altura do console"
      aria-valuenow={height}
      aria-valuemin={MIN_HEIGHT}
      aria-valuemax={maxHeight()}
      tabIndex={0}
      className={styles.handle}
      onPointerDown={handlePointerDown}
      onKeyDown={handleKeyDown}
    />
  )
}

export default ConsoleResizeHandle
