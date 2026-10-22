import { createContext } from 'react'

// Separado do SessionProvider pelo mesmo motivo do ConsoleContext: o Fast Refresh do Vite.
export const SessionContext = createContext(null)
