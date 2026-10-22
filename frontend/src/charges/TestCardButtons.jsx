import Button from '../components/Button.jsx'
import { TEST_CARDS } from './testCards.js'
import styles from './TestCardButtons.module.css'

function TestCardButtons({ onPick }) {
  return (
    <fieldset className={styles.group}>
      <legend className={styles.legend}>Cartões de teste</legend>
      <div className={styles.buttons}>
        {TEST_CARDS.map((testCard) => (
          <Button key={testCard.id} variant="secondary" size="sm" onClick={() => onPick(testCard)}>
            {testCard.label}
          </Button>
        ))}
      </div>
      <p className={styles.hint}>Preenchem o cartão e a moeda. O valor fica por sua conta.</p>
    </fieldset>
  )
}

export default TestCardButtons
