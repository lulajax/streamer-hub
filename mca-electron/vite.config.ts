import { defineConfig } from 'vite'
import path from 'path'
import electron from 'vite-plugin-electron'
import renderer from 'vite-plugin-electron-renderer'

const projectRoot = __dirname
const rendererRoot = path.join(projectRoot, 'src/renderer')

export default defineConfig(async () => {
  const vue = (await import('@vitejs/plugin-vue')).default
  return {
    plugins: [
      vue(),
      electron([
        {
          entry: path.join(projectRoot, 'src/main/index.ts'),
          onstart(options) {
            const env = { ...process.env }
            delete env.ELECTRON_RUN_AS_NODE
            options.startup(['.', '--no-sandbox'], { env })
          },
          vite: {
            build: {
              sourcemap: false,
              minify: false,
              outDir: path.join(projectRoot, 'dist/main'),
              rollupOptions: {
                external: ['electron'],
              },
            },
          },
        },
        {
          entry: path.join(projectRoot, 'src/preload/index.ts'),
          onstart(options) {
            options.reload()
          },
          vite: {
            build: {
              sourcemap: false,
              minify: false,
              outDir: path.join(projectRoot, 'dist/preload'),
              rollupOptions: {
                external: ['electron'],
              },
            },
          },
        },
      ]),
      renderer(),
    ],
    resolve: {
      alias: {
        '@': rendererRoot,
      },
    },
    root: rendererRoot,
    base: './',
    build: {
      outDir: path.join(projectRoot, 'dist/renderer'),
      emptyOutDir: true,
      sourcemap: false,
    },
    server: {
      port: 5173,
    },
  }
})
