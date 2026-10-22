import { Link, useSearchParams } from 'react-router'
import ChargeTable from '../charges/ChargeTable.jsx'
import { useChargePage } from '../charges/useChargePage.js'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Pagination from '../components/Pagination.jsx'
import styles from './ChargesPage.module.css'

// A página fica na URL para sobreviver a um recarregamento e ao botão voltar, depois de abrir uma cobrança. Na URL
// ela começa em 1, que é como as pessoas contam. Para a API, começa em 0.
function pageFromSearch(searchParams) {
  const number = Number.parseInt(searchParams.get('pagina'), 10)
  return Number.isInteger(number) && number > 1 ? number - 1 : 0
}

function ChargesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = pageFromSearch(searchParams)
  const { data, error, loading, reload } = useChargePage(page)

  function goToPage(next) {
    setSearchParams(next === 0 ? {} : { pagina: String(next + 1) })
  }

  function renderContent() {
    if (!data) {
      if (loading) {
        return <p className={styles.muted}>Carregando cobranças…</p>
      }
      return (
        <div role="alert" className={styles.error}>
          <p>Não deu para carregar as cobranças. {error.message}</p>
          <Button variant="secondary" size="sm" onClick={reload}>
            Tentar de novo
          </Button>
        </div>
      )
    }

    if (data.totalItems === 0) {
      return (
        <Card>
          <p className={styles.muted}>Nenhuma cobrança ainda.</p>
          <p>
            <Link to="/cobrancas/nova">Criar a primeira cobrança</Link>
          </p>
        </Card>
      )
    }

    return (
      <>
        <ChargeTable charges={data.items} />
        <div className={styles.footer}>
          <p className={styles.muted}>
            {data.totalItems} {data.totalItems === 1 ? 'cobrança' : 'cobranças'}
          </p>
          <Pagination page={page} totalPages={data.totalPages} onChange={goToPage} disabled={loading} />
        </div>
      </>
    )
  }

  return (
    <>
      <div className={styles.header}>
        <h1>Cobranças</h1>
        <Button variant="secondary" size="sm" onClick={reload} disabled={loading}>
          {loading ? 'Atualizando…' : 'Atualizar'}
        </Button>
      </div>
      <div className={styles.content}>{renderContent()}</div>
    </>
  )
}

export default ChargesPage
