import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  optimizeDeps: {
    exclude: ["elkjs"],
    include: ["elkjs/lib/elk.bundled.js"],
  },

  build: {
    commonjsOptions: {
      transformMixedEsModules: true,
    },
    rollupOptions: {
      external: (id) => {
        // web-worker를 external로 처리
        return id === "web-worker";
      },
    },
  },

  define: {
    global: "globalThis",
  },

  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@components": path.resolve(__dirname, "./src/components"),
      "@pages": path.resolve(__dirname, "./src/pages"),
      "@api": path.resolve(__dirname, "./src/api"),
      "@context": path.resolve(__dirname, "./src/context"),
      "@styles": path.resolve(__dirname, "./src/styles"),
      "@assets": path.resolve(__dirname, "./src/assets"),
      "@stores": path.resolve(__dirname, "./src/stores"),
      "@utils": path.resolve(__dirname, "./src/utils"),
      "@hooks": path.resolve(__dirname, "./src/hooks"),
      "@services": path.resolve(__dirname, "./src/services"),
      "@types": path.resolve(__dirname, "./src/types"),
      "@config": path.resolve(__dirname, "./src/config"),
      "@assets": path.resolve(__dirname, "./src/assets"),
      "@styles": path.resolve(__dirname, "./src/styles"),
    },
  },

  server: {
    host: true,
    port: 5173,

    /**
     * ⚠️ 중요!
     * axiosInstance baseURL = "/api/v1"
     * → 따라서 proxy 경로도 "/api/v1" 이어야 한다.
     */
    proxy: {
      "/api/v1": {
        // target: "http://192.168.0.42:8081", // 백엔드 스프링 서버 주소
        target: "http://172.30.1.16:8081",
        //target: "http://localhost:8081",
        changeOrigin: true,
        secure: false,
      },
      // 정적 리소스 (이미지 등) 프록시
      "/uploads": {
        // target: "http://192.168.0.42:8081",
        target: "http://172.30.1.16:8081",
        //target: "http://localhost:8081",
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
