import { Link } from 'react-router'
import Badge from '../components/Badge.jsx'
import { chargeStatus } from '../charges/chargeStatus.js'
import styles from './ConcurrencyScoreboard.module.css'

function outcomeKind(outcome) {
  if (outcome.ok) {
    return 'success'
  }
  return outcome.status === 409 ? 'conflict' : 'other'
}

// Os 409 vêm de duas garantias diferentes, e a mensagem diz qual barrou cada pedido: o lock otimista, quando dois
// pedidos gravam ao mesmo tempo, ou a máquina de estados, quando a captura chega depois de a cobrança já estar
// capturada.
function countConflictMessages(outcomes) {
  const counts = new Map()
  for (const outcome of outcomes) {
    if (outcomeKind(outcome) === 'conflict') {
      counts.set(outcome.message, (counts.get(outcome.message) ?? 0) + 1)
    }
  }
  return [...counts]
}

function ConcurrencyScoreboard({ outcomes, durationMs, finalCharge }) {
  const kinds = outcomes.map(outcomeKind)
  const successes = kinds.filter((kind) => kind === 'success').length
  const conflicts = kinds.filter((kind) => kind === 'conflict').length
  const others = kinds.length - successes - conflicts
  const captureEvents = finalCharge.events.filter((event) => event.toStatus === 'CAPTURED').length
  const asExpected = successes === 1 && conflicts === outcomes.length - 1 && captureEvents === 1

  return (
    <div className={styles.scoreboard}>
      <p>
        {asExpected ? (
          <Badge tone="success">Como esperado</Badge>
        ) : (
          <Badge tone="danger">Resultado inesperado</Badge>
        )}
      </p>

      <dl className={styles.stats}>
        <div className={`${styles.stat} ${styles.success}`}>
          <dt>Sucesso</dt>
          <dd>{successes}</dd>
        </div>
        <div className={`${styles.stat} ${styles.conflict}`}>
          <dt>Conflito (409)</dt>
          <dd>{conflicts}</dd>
        </div>
        <div className={`${styles.stat} ${styles.other}`}>
          <dt>Outros</dt>
          <dd>{others}</dd>
        </div>
      </dl>

      {/* Um quadrado por captura, na ordem em que foram disparadas. Os números acima já dizem o mesmo para quem
          usa leitor de tela. */}
      <div className={styles.attempts} aria-hidden="true">
        {kinds.map((kind, index) => (
          <span key={index} className={`${styles.attempt} ${styles[kind]}`} title={`Captura ${index + 1}`} />
        ))}
      </div>

      {conflicts > 0 && (
        <ul className={styles.messages}>
          {countConflictMessages(outcomes).map(([message, count]) => (
            <li key={message}>
              <strong>{count} ×</strong> {message}
            </li>
          ))}
        </ul>
      )}

      <p className={styles.summary}>
        {outcomes.length} capturas em {durationMs} ms. Estado final: {chargeStatus(finalCharge.status).label}, com{' '}
        {captureEvents} {captureEvents === 1 ? 'evento' : 'eventos'} de captura na linha do tempo.{' '}
        <Link to={`/cobrancas/${finalCharge.id}`}>Ver a cobrança</Link>
      </p>
    </div>
  )
}

export default ConcurrencyScoreboard
