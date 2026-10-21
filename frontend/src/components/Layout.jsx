import { Link, NavLink, Outlet } from 'react-router'
import Logo from './Logo.jsx'
import ThemeToggle from './ThemeToggle.jsx'
import styles from './Layout.module.css'

function navLinkClass({ isActive }) {
  return isActive ? `${styles.navLink} ${styles.active}` : styles.navLink
}

function Layout() {
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

      <main className={styles.main}>
        <Outlet />
      </main>

      <footer className={styles.footer}>
        <p>Projeto de estudo, sem vínculo com instituições financeiras.</p>
        <Link to="/componentes">Componentes</Link>
      </footer>
    </div>
  )
}

export default Layout
