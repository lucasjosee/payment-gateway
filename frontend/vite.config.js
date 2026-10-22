import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Na AWS, o CloudFront manda o que começa com /api para a EC2 e o resto para o S3, e o navegador vê um endereço
    // só. Em desenvolvimento, o Vite faz o mesmo papel. O caminho segue inteiro, com o /api, porque o Spring Boot
    // também atende sob esse prefixo.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        // Se a API cai no meio de um stream, o proxy não avisa o navegador. Derrubar a resposta deixa o front reconectar.
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            proxyRes.on('close', () => {
              if (!proxyRes.complete) {
                res.destroy()
              }
            })
          })
        },
      },
    },
  },
})
