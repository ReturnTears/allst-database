import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/graphql': {
        target: 'http://localhost:8089',
        changeOrigin: true
      }
    }
  }
})
