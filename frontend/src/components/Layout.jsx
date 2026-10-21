import { useEffect, useRef } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router'
import ConsolePanel from '../console/ConsolePanel.jsx'
import ApiStatus from './ApiStatus.jsx'
import Logo from './Logo.jsx'
import ThemeToggle from './ThemeToggle.jsx'
import styles from './Layout.module.css'

function navLinkClass({ isActive }) {
  return isActive ? `${styles.navLink} ${styles.active}` : styles.navLink
}

function Layout() {
  const { pathname } = useLocation()
  const contentRef = useRef(null)

  // Quem rola é a faixa do conteúdo, e não a janela, então o navegador não volta ao topo sozinho quando a página
  // muda. Sem isto, abrir uma cobrança no fim de uma lista longa mostraria o detalhe já rolado para baixo.
  useEffect(() => {
    contentRef.current.scrollTo(0, 0)
  }, [pathname])

  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <Link to="/" className={styles.brand}>
          <Logo />
        </Link>
        <nav className={styles.nav} aria-label="Principal">
          {/* O end impede que "Cobranças" fique marcada também em /cobrancas/nova, que tem link próprio. */}
          <NavLink to="/cobrancas" end className={navLinkClass}>
            Cobranças
          </NavLink>
          <NavLink to="/cobrancas/nova" className={navLinkClass}>
            Nova cobrança
          </NavLink>
          <NavLink to="/laboratorio" className={navLinkClass}>
            Laboratório
          </NavLink>
        </nav>
        <ThemeToggle />
      </header>

      <div ref={contentRef} className={styles.content}>
        <main className={styles.main}>
          <Outlet />
        </main>

        <footer className={styles.footer}>
          <p>Projeto de estudo, sem vínculo com instituições financeiras.</p>
          <div className={styles.footerLinks}>
            <ApiStatus />
            <Link to="/componentes">Componentes</Link>
          </div>
        </footer>
      </div>

      <ConsolePanel />
    </div>
  )
}

export default Layout
