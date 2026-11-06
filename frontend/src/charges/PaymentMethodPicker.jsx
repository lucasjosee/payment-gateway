import Button from '../components/Button.jsx'
import ButtonGroup from '../components/ButtonGroup.jsx'

const METHODS = [
  { id: 'card', label: 'Cartão' },
  { id: 'pix', label: 'Pix' },
]

function PaymentMethodPicker({ value, onChange }) {
  return (
    <ButtonGroup legend="Meio de pagamento">
      {METHODS.map(({ id, label }) => (
        <Button key={id} variant="secondary" size="sm" aria-pressed={value === id} onClick={() => onChange(id)}>
          {label}
        </Button>
      ))}
    </ButtonGroup>
  )
}

export default PaymentMethodPicker
