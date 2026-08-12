import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // O manifesto e escrito a mao em public/manifest.webmanifest.
      manifest: false,
      registerType: 'autoUpdate',
      injectRegister: 'auto',
      workbox: {
        // Só a casca do app entra no precache — nunca a API.
        globPatterns: ['**/*.{js,css,html,svg,png,woff2}'],
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api/],
        cleanupOutdatedCaches: true,
      },
      // Em dev o service worker fica fora do caminho: nada de servir tela velha
      // enquanto alguem edita o código.
      devOptions: { enabled: false },
    }),
  ],
  server: {
    port: 5173,
    // Evita CORS no dev: o front chama /api e o Vite repassa para o backend.
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
