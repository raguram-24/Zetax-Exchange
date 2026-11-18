import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Dev server proxies to zetax-gateway (port 8080), so the browser only talks to localhost:5173.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',      // REST: place / cancel / history (D-14)
      '/admin': 'http://localhost:8080',    // Admin API: symbols (D-20)
      '/ws': { target: 'ws://localhost:8080', ws: true }, // STOMP WebSocket: live updates (D-14)
    },
  },
})
