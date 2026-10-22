import { Navigate, Outlet } from 'react-router'
import { useSession } from './useSession.js'

// Envolve as rotas que precisam de uma API key. Sem sessão, a pessoa volta para a tela de boas-vindas.
function RequireSession() {
  const { merchant } = useSession()
  return merchant ? <Outlet /> : <Navigate to="/" replace />
}

export default RequireSession
