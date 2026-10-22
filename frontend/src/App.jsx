import { Route, Routes } from 'react-router'
import Layout from './components/Layout.jsx'
import ChargeDetailPage from './pages/ChargeDetailPage.jsx'
import ChargesPage from './pages/ChargesPage.jsx'
import ComponentsPage from './pages/ComponentsPage.jsx'
import LabPage from './pages/LabPage.jsx'
import NewChargePage from './pages/NewChargePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import WelcomePage from './pages/WelcomePage.jsx'
import RequireSession from './session/RequireSession.jsx'

function App() {
  // O React Router escolhe a rota mais específica, e não a primeira da lista. Por isso /cobrancas/nova nunca é
  // confundida com o id "nova" de /cobrancas/:id, seja qual for a ordem das linhas.
  return (
    <Routes>
      {/* Uma rota sem path só embrulha as outras: o Layout aparece em todas, e o <Outlet> dele mostra a página. */}
      <Route element={<Layout />}>
        <Route path="/" element={<WelcomePage />} />
        <Route element={<RequireSession />}>
          <Route path="/cobrancas" element={<ChargesPage />} />
          <Route path="/cobrancas/nova" element={<NewChargePage />} />
          <Route path="/cobrancas/:id" element={<ChargeDetailPage />} />
          <Route path="/laboratorio" element={<LabPage />} />
        </Route>
        <Route path="/componentes" element={<ComponentsPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default App
