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
      '/api': 'http://localhost:8080',
    },
  },
})
