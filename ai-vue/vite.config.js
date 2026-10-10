import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: false })],
      types: [{ from: '@element-plus/icons-vue', names: ['*'] }],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:1235',
        changeOrigin: true
      },
      '/files': {
        target: 'http://localhost:1235',
        changeOrigin: true
      },
      '/file': {
        target: 'http://localhost:1235',
        changeOrigin: true
      },
    },
  },
})
