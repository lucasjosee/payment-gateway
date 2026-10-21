import Badge from '../components/Badge.jsx'
import Button from '../components/Button.jsx'
import Card from '../components/Card.jsx'
import Input from '../components/Input.jsx'
import styles from './ComponentsPage.module.css'

const THEMES = [
  { id: 'light', name: 'Tema claro' },
  { id: 'dark', name: 'Tema escuro' },
]

function ComponentsPage() {
  return (
    <>
      <h1>Componentes</h1>
      <p className={styles.intro}>
        Os componentes base nos dois temas. Cores, espaçamentos e tipografia vêm todos de src/styles/tokens.css.
      </p>

      <div className={styles.panels}>
        {/* O data-theme vale só para o painel: cada um mostra um tema, seja qual for o do resto da página. */}
        {THEMES.map((theme) => (
          <section key={theme.id} data-theme={theme.id} className={styles.panel}>
            <h2>{theme.name}</h2>

            <h3>Botões</h3>
            <div className={styles.row}>
              <Button>Criar cobrança</Button>
              <Button variant="secondary">Capturar</Button>
              <Button variant="ghost">Copiar como curl</Button>
            </div>
            <div className={styles.row}>
              <Button size="sm">Capturar</Button>
              <Button variant="secondary" size="sm">
                Atualizar
              </Button>
              <Button variant="ghost" size="sm">
                Limpar
              </Button>
            </div>
            <div className={styles.row}>
              <Button disabled>Criar cobrança</Button>
              <Button variant="secondary" disabled>
                Estornar
              </Button>
            </div>

            <h3>Campos</h3>
            <div className={styles.stack}>
              <Input label="Valor" placeholder="150,00" hint="Em reais. A API recebe o valor em centavos." />
              <Input label="Valor" defaultValue="0" error="Informe um valor maior que zero." />
              <Input label="Lojista" defaultValue="Loja Demo" disabled />
            </div>

            <h3>Badges</h3>
            <div className={styles.row}>
              <Badge tone="info">Autorizada</Badge>
              <Badge tone="success">Capturada</Badge>
              <Badge tone="warning">Em andamento</Badge>
              <Badge tone="danger">Recusada</Badge>
              <Badge>Cancelada</Badge>
            </div>

            <h3>Card</h3>
            <Card title="Cobrança de R$ 150,00">
              <Badge tone="info">Autorizada</Badge>
              <p className={styles.muted}>Cartão com final 4242, criada às 14:19.</p>
              <div className={styles.row}>
                <Button size="sm">Capturar</Button>
                <Button variant="ghost" size="sm">
                  Cancelar
                </Button>
              </div>
            </Card>
          </section>
        ))}
      </div>
    </>
  )
}

export default ComponentsPage
