import Button from '../components/Button.jsx'
import ButtonGroup from '../components/ButtonGroup.jsx'
import { TEST_CARDS } from './testCards.js'

function TestCardButtons({ onPick }) {
  return (
    <ButtonGroup legend="Cartões de teste" hint="Preenchem o cartão e a moeda. O valor fica por sua conta.">
      {TEST_CARDS.map((testCard) => (
        <Button key={testCard.id} variant="secondary" size="sm" onClick={() => onPick(testCard)}>
          {testCard.label}
        </Button>
      ))}
    </ButtonGroup>
  )
}

export default TestCardButtons
