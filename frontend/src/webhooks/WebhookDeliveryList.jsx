import { Link, useSearchParams } from 'react-router'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import LoadError from '../components/LoadError.jsx'
import Pagination from '../components/Pagination.jsx'
import WebhookDeliveryTable from './WebhookDeliveryTable.jsx'
import { useWebhookDeliveryPage } from './useWebhookDeliveryPage.js'
import styles from './WebhookDeliveryList.module.css'

// Como na lista de cobranças, a página fica na URL, para sobreviver a um recarregamento e ao botão voltar depois de
// abrir uma cobrança. Na URL ela começa em 1 e, para a API, em 0.
function pageFromSearch(searchParams) {
  const number = Number.parseInt(searchParams.get('pagina'), 10)
  return Number.isInteger(number) && number > 1 ? number - 1 : 0
}

function WebhookDeliveryList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = pageFromSearch(searchParams)
  const { data, error, loading, reload } = useWebhookDeliveryPage(page)

  function goToPage(next) {
    setSearchParams(next === 0 ? {} : { pagina: String(next + 1) })
  }

  function renderContent() {
    if (!data) {
      if (loading) {
        return <p className={styles.muted}>Carregando as entregas…</p>
      }
      return <LoadError message="Não deu para carregar as entregas." error={error} onRetry={reload} />
    }

    if (data.totalItems === 0) {
      return (
        <Card>
          <p className={styles.muted}>Nenhuma entrega ainda. Cada mudança de status de uma cobrança gera uma.</p>
          <p>
            <Link to="/cobrancas/nova">Criar uma cobrança</Link>
          </p>
        </Card>
      )
    }

    // Acontece quando alguém edita o número da página na URL. A API responde 200 com a lista vazia.
    if (data.items.length === 0) {
      return (
        <Card>
          <p className={styles.muted}>
            A página {page + 1} não existe. As suas entregas cabem em {data.totalPages}{' '}
            {data.totalPages === 1 ? 'página' : 'páginas'}.
          </p>
          <p>
            <Link to="/webhooks">Ir para a primeira página</Link>
          </p>
        </Card>
      )
    }

    return (
      <>
        <WebhookDeliveryTable deliveries={data.items} />
        <div className={styles.footer}>
          <p className={styles.muted}>
            {data.totalItems} {data.totalItems === 1 ? 'entrega' : 'entregas'}
          </p>
          <Pagination page={page} totalPages={data.totalPages} onChange={goToPage} disabled={loading} />
        </div>
      </>
    )
  }

  return (
    <section>
      <div className={styles.header}>
        <h2>Entregas</h2>
        <Button variant="secondary" size="sm" onClick={reload} disabled={loading}>
          {loading ? 'Atualizando…' : 'Atualizar'}
        </Button>
      </div>
      <div className={styles.content}>{renderContent()}</div>
    </section>
  )
}

export default WebhookDeliveryList
