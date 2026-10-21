import { Route, Routes } from 'react-router'
import ChargeDetailPage from './pages/ChargeDetailPage.jsx'
import ChargesPage from './pages/ChargesPage.jsx'
import LabPage from './pages/LabPage.jsx'
import NewChargePage from './pages/NewChargePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import WelcomePage from './pages/WelcomePage.jsx'

function App() {
  // O React Router escolhe a rota mais específica, e não a primeira da lista. Por isso /cobrancas/nova nunca é
  // confundida com o id "nova" de /cobrancas/:id, seja qual for a ordem das linhas.
  return (
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/cobrancas" element={<ChargesPage />} />
      <Route path="/cobrancas/nova" element={<NewChargePage />} />
      <Route path="/cobrancas/:id" element={<ChargeDetailPage />} />
      <Route path="/laboratorio" element={<LabPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
