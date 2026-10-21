import { createContext } from 'react'

// Fica num arquivo separado do ConsoleProvider porque o Fast Refresh do Vite só recarrega sem perder o estado
// arquivos que exportam apenas componentes.
export const ConsoleContext = createContext(null)
