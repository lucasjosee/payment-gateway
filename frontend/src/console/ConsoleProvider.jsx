import { useEffect, useReducer } from 'react'
import { subscribeToRequests } from '../api/client.js'
import { ConsoleContext } from './ConsoleContext.js'
import { consoleReducer, initialConsoleState } from './consoleReducer.js'

function ConsoleProvider({ children }) {
  const [state, dispatch] = useReducer(consoleReducer, initialConsoleState)

  useEffect(() => subscribeToRequests((entry) => dispatch({ type: 'entryUpdated', entry })), [])

  return <ConsoleContext value={state}>{children}</ConsoleContext>
}

export default ConsoleProvider
