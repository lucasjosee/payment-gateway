import { useParams } from 'react-router'
import PixPayment from '../charges/PixPayment.jsx'
import { usePixPayment } from '../charges/usePixPayment.js'
import Card from '../components/Card.jsx'
import LoadError from '../components/LoadError.jsx'
import Logo from '../components/Logo.jsx'
import styles from './PixPaymentPage.module.css'

function PixPaymentPage() {
  const { id } = useParams()
  const { pix, loadError, payError, paying, pay, reload } = usePixPayment(id)

  function renderContent() {
    // Um id que nem tem formato de UUID dá 400, e para quem abriu um link quebrado é o mesmo que não existir.
    if (loadError?.status === 404 || loadError?.status === 400) {
      return (
        <Card title="Pix não encontrado">
          <p className={styles.muted}>Este link não leva a nenhum Pix. Peça um novo link a quem fez a cobrança.</p>
        </Card>
      )
    }
    if (pix) {
      return <PixPayment pix={pix} paying={paying} error={payError} onPay={pay} />
    }
    if (loadError) {
      return <LoadError message="Não deu para carregar o Pix." error={loadError} onRetry={reload} />
    }
    return <p className={styles.muted}>Carregando o Pix…</p>
  }

  return (
    <div className={styles.page}>
      <p className={styles.notice}>Simulação: nenhum dinheiro de verdade é movimentado nesta página.</p>
      <div className={styles.column}>
        <header className={styles.header}>
          <Logo />
          <span className={styles.muted}>Simulador de Pix</span>
        </header>
        <main className={styles.main}>{renderContent()}</main>
        <footer className={styles.muted}>Projeto de estudo, sem vínculo com instituições financeiras.</footer>
      </div>
    </div>
  )
}

export default PixPaymentPage
