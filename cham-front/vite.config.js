import { defineConfig, loadEnv } from 'vite';
import svgr from 'vite-plugin-svgr';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    server: {
      host: '0.0.0.0',
      port: 5173,
      strictPort: true,
      cors: true,
      proxy: {
        '/cham': {
          target: env.VITE_API_URL,
          changeOrigin: true,
          secure: false,
        },
      },
    },
    plugins: [
      svgr({
        svgrOptions: {
          // named export 사용
          exportType: 'named', // 또는 exportType: 'named'
        },
        // 혹시 이전에 exportAsDefault: true 썼다면 제거/false로
        // exportAsDefault: false,
      }),
      react(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    define: {
      'import.meta.env.VITE_API_URL': JSON.stringify(env.VITE_API_URL),
    },
  };
});
