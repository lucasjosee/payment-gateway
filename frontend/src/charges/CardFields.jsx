import Input from '../components/Input.jsx'
import styles from './CardFields.module.css'

function CardFields({ card, onChange }) {
  return (
    <>
      {/* Sem autoComplete="off", o navegador poderia oferecer um cartão de verdade salvo nele. */}
      <Input
        label="Número do cartão"
        value={card.number}
        onChange={(event) => onChange({ number: event.target.value })}
        inputMode="numeric"
        autoComplete="off"
        required
      />
      <div className={styles.row}>
        <Input
          label="Validade"
          value={card.expiry}
          onChange={(event) => onChange({ expiry: event.target.value })}
          placeholder="AAAA-MM"
          pattern="\d{4}-\d{2}"
          autoComplete="off"
          required
        />
        <Input
          label="CVV"
          value={card.cvv}
          onChange={(event) => onChange({ cvv: event.target.value })}
          inputMode="numeric"
          maxLength={4}
          autoComplete="off"
          required
        />
      </div>
    </>
  )
}

export default CardFields
