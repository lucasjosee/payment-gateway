import '@fontsource-variable/figtree'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import App from './App.jsx'
import ConsoleProvider from './console/ConsoleProvider.jsx'
import SessionProvider from './session/SessionProvider.jsx'
import './styles/tokens.css'
import './styles/global.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ConsoleProvider>
      <SessionProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </SessionProvider>
    </ConsoleProvider>
  </StrictMode>,
)
