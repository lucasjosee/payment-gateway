import CopyButton from '../components/CopyButton.jsx'
import PixQrCode from './PixQrCode.jsx'
import styles from './PixPaymentLink.module.css'

// O endereço sai da própria página, então o link funciona igual no notebook e no CloudFront.
function PixPaymentLink({ chargeId }) {
  const url = `${window.location.origin}/pix/${chargeId}`

  return (
    <div className={styles.link}>
      <p className={styles.label}>Link de pagamento</p>
      <PixQrCode text={url} />
      <a href={url} target="_blank" rel="noreferrer" className={styles.url}>
        {url}
      </a>
      <CopyButton variant="secondary" size="sm" text={url}>
        Copiar link
      </CopyButton>
    </div>
  )
}

export default PixPaymentLink
