import { Link } from 'react-router'
import styles from './NotFoundPage.module.css'

function NotFoundPage() {
  return (
    <>
      <h1>Página não encontrada</h1>
      <p className={styles.intro}>
        Este endereço não leva a nenhuma tela do Ponte. <Link to="/">Voltar ao início</Link>
      </p>
    </>
  )
}

export default NotFoundPage
