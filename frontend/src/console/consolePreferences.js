// Só preferências de tela: nenhuma requisição é guardada no navegador.
const STORAGE_KEY = 'ponte-console'

export const DEFAULT_PREFERENCES = { open: true, height: 240, filter: 'all' }

export function loadPreferences() {
  try {
    return { ...DEFAULT_PREFERENCES, ...JSON.parse(localStorage.getItem(STORAGE_KEY)) }
  } catch {
    // Valor corrompido ou armazenamento bloqueado: o console abre no padrão.
    return DEFAULT_PREFERENCES
  }
}

export function savePreferences(preferences) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
  } catch {
    // Sem armazenamento, as preferências valem só até a página ser recarregada.
  }
}
