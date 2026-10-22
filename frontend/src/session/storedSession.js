// A sessão fica no sessionStorage: sobrevive a um recarregamento, mas cada aba tem a sua e ela some quando a aba é
// fechada.
const STORAGE_KEY = 'ponte-session'

export function loadStoredSession() {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY))
  } catch {
    // Valor corrompido ou armazenamento bloqueado: a pessoa entra de novo.
    return null
  }
}

export function storeSession(session) {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch {
    // Sem armazenamento, a sessão vale só até a página ser recarregada.
  }
}

export function clearStoredSession() {
  try {
    sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // Se o armazenamento está bloqueado, nada foi guardado nele.
  }
}
