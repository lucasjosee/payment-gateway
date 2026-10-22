import { Link } from 'react-router'
import Badge from '../components/Badge.jsx'
import { chargeStatus, declineReasonText } from './chargeStatus.js'
import { formatCents } from './money.js'
import styles from './ChargeTable.module.css'

// Com segundos, para dar para conferir a ordem de cobranças criadas em sequência.
const DATE_FORMAT = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'medium' })

function ChargeTable({ charges }) {
  return (
    <div className={styles.scroll}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">Cobrança</th>
            <th scope="col">Criada em</th>
            <th scope="col">Status</th>
            <th scope="col" className={styles.amount}>
              Valor
            </th>
            <th scope="col">Cartão</th>
          </tr>
        </thead>
        <tbody>
          {charges.map((charge) => {
            const { label, tone } = chargeStatus(charge.status)
            return (
              <tr key={charge.id}>
                {/* Os 8 primeiros caracteres bastam para diferenciar as cobranças na tabela. O ID inteiro fica no
                    title e na página de detalhe, para onde o link leva. */}
                <td>
                  <Link to={`/cobrancas/${charge.id}`} className={styles.id} title={charge.id}>
                    {charge.id.slice(0, 8)}
                  </Link>
                </td>
                <td className={styles.muted}>{DATE_FORMAT.format(new Date(charge.createdAt))}</td>
                <td title={charge.declineReason ? declineReasonText(charge.declineReason) : undefined}>
                  <Badge tone={tone}>{label}</Badge>
                </td>
                <td className={styles.amount}>{formatCents(charge.amountCents, charge.currency)}</td>
                <td className={styles.muted}>•••• {charge.cardLast4}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default ChargeTable
