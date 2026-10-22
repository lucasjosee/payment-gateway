import { useMemo, useState } from 'react'
import { api, setApiKey } from '../api/client.js'
import { SessionContext } from './SessionContext.js'
import { clearStoredSession, loadStoredSession, storeSession } from './storedSession.js'

// Roda no primeiro render do provider, que acontece antes dos efeitos de qualquer página. Assim, depois de um
// recarregamento, a chave já está no client quando as páginas fazem as primeiras requisições.
function restoreSession() {
  const stored = loadStoredSession()
  setApiKey(stored?.apiKey ?? null)
  return stored?.merchant ?? null
}

// A chave vai para o client e para o sessionStorage, mas não para o estado do React. O estado guarda só os dados do
// lojista, que são o que a interface mostra.
function SessionProvider({ children }) {
  const [merchant, setMerchant] = useState(restoreSession)

  const value = useMemo(() => {
    function startSession(apiKey, merchant) {
      setApiKey(apiKey)
      storeSession({ apiKey, merchant })
      setMerchant(merchant)
    }

    return {
      merchant,
      startSession,
      // Antes de virar sessão, a chave passa por GET /merchants/me. Uma chave que a API recusa nunca é guardada.
      async signIn(apiKey) {
        setApiKey(apiKey)
        try {
          startSession(apiKey, await api.get('/merchants/me'))
        } catch (error) {
          setApiKey(null)
          throw error
        }
      },
      signOut() {
        clearStoredSession()
        setApiKey(null)
        setMerchant(null)
      },
    }
  }, [merchant])

  return <SessionContext value={value}>{children}</SessionContext>
}

export default SessionProvider
