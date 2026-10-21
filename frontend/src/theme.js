// A mesma chave é lida por um <script> no index.html, que aplica o tema salvo antes de a página aparecer. Sem ele,
// quem escolheu o tema escuro veria a página clara por um instante a cada carregamento.
const STORAGE_KEY = 'ponte-theme'

const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)')

function savedTheme() {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    // Alguns navegadores bloqueiam o armazenamento, como em certas janelas anônimas. Aí vale o tema do sistema.
    return null
  }
}

export function currentTheme() {
  return savedTheme() ?? (systemPrefersDark.matches ? 'dark' : 'light')
}

export function chooseTheme(theme) {
  document.documentElement.dataset.theme = theme
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // Sem armazenamento, a escolha vale só até a página ser recarregada.
  }
}

// Avisa quando o tema do sistema muda. Só faz diferença para quem ainda não escolheu um tema pelo botão.
export function onSystemThemeChange(callback) {
  systemPrefersDark.addEventListener('change', callback)
  return () => systemPrefersDark.removeEventListener('change', callback)
}
