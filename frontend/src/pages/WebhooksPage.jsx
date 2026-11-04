import LoadError from '../components/LoadError.jsx'
import SigningSecretCard from '../webhooks/SigningSecretCard.jsx'
import WebhookDeliveryList from '../webhooks/WebhookDeliveryList.jsx'
import WebhookEndpointForm from '../webhooks/WebhookEndpointForm.jsx'
import { useWebhookEndpoint } from '../webhooks/useWebhookEndpoint.js'
import styles from './WebhooksPage.module.css'

function WebhooksPage() {
  const { endpoint, error, loading, update, reload } = useWebhookEndpoint()

  // O formulário só aparece depois da resposta, e assim o campo já nasce com a URL cadastrada.
  function renderContent() {
    if (loading) {
      return <p className={styles.muted}>Carregando o webhook…</p>
    }
    if (error) {
      return <LoadError message="Não deu para carregar o webhook." error={error} onRetry={reload} />
    }
    return (
      <>
        <WebhookEndpointForm registeredUrl={endpoint?.url ?? null} onRegistered={update} />
        {/* O segredo nasce com o endpoint, e sem ele nenhuma entrega é gerada. */}
        {endpoint && (
          <>
            <SigningSecretCard secret={endpoint.signingSecret} />
            <WebhookDeliveryList />
          </>
        )}
      </>
    )
  }

  return (
    <>
      <h1>Webhooks</h1>
      <p className={styles.intro}>
        A API avisa o servidor da loja quando uma cobrança muda de status. Sem um servidor para isso, use o receptor de
        teste, que faz esse papel dentro da própria API.
      </p>
      <div className={styles.content}>{renderContent()}</div>
    </>
  )
}

export default WebhooksPage
