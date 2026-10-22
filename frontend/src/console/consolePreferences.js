// Só preferências de tela: nenhuma requisição é guardada no navegador.
const STORAGE_KEY = 'ponte-console'
const DEFAULT_HEIGHT = 240

// Num notebook de tela baixa, 240px de console deixariam pouco espaço para a página, então a altura inicial é no
// máximo 30% da janela. Depois que a pessoa arrasta a alça, vale a altura que ela escolheu.
function defaultPreferences() {
  return { open: true, height: Math.min(DEFAULT_HEIGHT, Math.round(window.innerHeight * 0.3)), filter: 'all' }
}

export function loadPreferences() {
  const defaults = defaultPreferences()
  try {
    return { ...defaults, ...JSON.parse(localStorage.getItem(STORAGE_KEY)) }
  } catch {
    // Valor corrompido ou armazenamento bloqueado: o console abre no padrão.
    return defaults
  }
}

export function savePreferences(preferences) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences))
  } catch {
    // Sem armazenamento, as preferências valem só até a página ser recarregada.
  }
}
