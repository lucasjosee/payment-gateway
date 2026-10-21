import { useContext } from 'react'
import { ConsoleContext } from './ConsoleContext.js'

export function useConsole() {
  const context = useContext(ConsoleContext)
  if (!context) {
    throw new Error('useConsole precisa ser usado dentro do ConsoleProvider.')
  }
  return context
}
