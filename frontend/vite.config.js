import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前端 5173 端口，/api 请求代理到后端 8080（后端若临时用 8081，改这里即可）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
