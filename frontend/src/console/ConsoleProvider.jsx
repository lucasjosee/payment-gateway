import { useEffect, useMemo, useReducer } from 'react'
import { subscribeToRequests } from '../api/client.js'
import { ConsoleContext } from './ConsoleContext.js'
import { consoleReducer, initialConsoleState } from './consoleReducer.js'

function ConsoleProvider({ children }) {
  const [state, dispatch] = useReducer(consoleReducer, initialConsoleState)

  useEffect(() => subscribeToRequests((entry) => dispatch({ type: 'entryUpdated', entry })), [])

  // Sem o useMemo, cada render do provider criaria um objeto novo, e todo componente que lê o contexto
  // renderizaria de novo mesmo sem nenhuma requisição nova.
  const value = useMemo(
    () => ({
      ...state,
      clear: () => dispatch({ type: 'cleared' }),
      highlight: (tag) => dispatch({ type: 'highlighted', tag }),
      selectTab: (tab) => dispatch({ type: 'tabSelected', tab }),
      showServerLogs: (requestId) => dispatch({ type: 'serverLogsRequested', requestId }),
      clearLogFilter: () => dispatch({ type: 'logFilterCleared' }),
    }),
    [state],
  )

  return <ConsoleContext value={value}>{children}</ConsoleContext>
}

export default ConsoleProvider
