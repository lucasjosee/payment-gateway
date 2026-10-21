import { useEffect, useState } from 'react'
import { chooseTheme, currentTheme, onSystemThemeChange } from '../theme.js'
import Button from './Button.jsx'
import styles from './ThemeToggle.module.css'

function ThemeToggle() {
  const [theme, setTheme] = useState(currentTheme)

  // Enquanto a pessoa não escolhe pelo botão, o CSS já faz a página seguir o sistema sozinha. Isto só mantém o
  // botão em dia quando o sistema troca de tema com a página aberta.
  useEffect(() => onSystemThemeChange(() => setTheme(currentTheme())), [])

  const isDark = theme === 'dark'

  function toggle() {
    const next = isDark ? 'light' : 'dark'
    chooseTheme(next)
    setTheme(next)
  }

  return (
    <Button variant="ghost" size="sm" aria-pressed={isDark} onClick={toggle}>
      <svg className={styles.icon} viewBox="0 0 24 24" aria-hidden="true">
        <path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z" />
      </svg>
      Tema escuro
    </Button>
  )
}

export default ThemeToggle
