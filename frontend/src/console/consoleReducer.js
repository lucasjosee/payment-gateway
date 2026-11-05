// Uma aba aberta por horas acumularia requisições sem fim. Passado o limite, as mais antigas saem. As consultas
// automáticas têm um limite só delas: dividindo o mesmo, em poucos minutos elas expulsariam, sem aparecer na tela, as
// requisições que a pessoa fez.
const MAX_ENTRIES = 200
const MAX_BACKGROUND_ENTRIES = 50

// highlightedTag é a tag das requisições que o console destaca, as do último experimento do laboratório.
export const initialConsoleState = { entries: [], highlightedTag: null, tab: 'requests', logFilter: null }

function addEntry(entries, entry) {
  const sameKind = entries.filter((current) => current.background === entry.background)
  const limit = entry.background ? MAX_BACKGROUND_ENTRIES : MAX_ENTRIES
  if (sameKind.length < limit) {
    return [...entries, entry]
  }
  return [...entries.filter((current) => current !== sameKind[0]), entry]
}

export function consoleReducer(state, action) {
  switch (action.type) {
    // Cada aviso do client traz a requisição inteira, no estado em que ela está. O reducer troca a versão antiga
    // pela nova, ou acrescenta a requisição se ainda não a conhece. Assim nada se perde se o primeiro aviso chegar
    // antes de o ConsoleProvider se inscrever, o que acontece de fato: o React roda os efeitos dos filhos antes dos
    // do pai, e o health do ApiStatus sai antes da inscrição.
    case 'entryUpdated': {
      const { entry } = action
      const known = state.entries.some((current) => current.id === entry.id)
      const entries = known
        ? state.entries.map((current) => (current.id === entry.id ? entry : current))
        : addEntry(state.entries, entry)
      return { ...state, entries }
    }
    case 'highlighted':
      return { ...state, highlightedTag: action.tag }
    case 'tabSelected':
      return { ...state, tab: action.tab }
    case 'serverLogsRequested':
      return { ...state, tab: 'server', logFilter: action.requestId }
    case 'logFilterCleared':
      return { ...state, logFilter: null }
    // Um pedido que ainda estava em andamento volta a aparecer quando termina, porque o reducer não o conhece mais.
    case 'cleared':
      return { ...state, entries: [], highlightedTag: null }
    default:
      return state
  }
}
