import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // no rewrite: let the proxy forward the full path (e.g. /api/customers)
      },
      '/account': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // no rewrite: forward /account/* to the Account service which is mounted
        // at the /account context path. Removing the broken regex avoids 500s.
      }
    }
  }
})
