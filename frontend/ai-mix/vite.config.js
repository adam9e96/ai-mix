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
      output: {
        // 라이브러리별 chunk 분리 (캐싱 최적화)
        // 라이브러리가 업데이트되지 않는 한 브라우저 캐시를 재활용
        manualChunks: {
          // React 코어 (거의 변경되지 않음)
          "vendor-react": ["react", "react-dom", "react-router-dom"],
          // 마크다운 렌더링 (Chat, QnaDetail에서 사용)
          "vendor-markdown": [
            "react-markdown",
            "remark-gfm",
            "rehype-raw",
            "react-syntax-highlighter",
          ],
          // ReactFlow 그래프 (QnaGraph에서 사용)
          "vendor-flow": ["reactflow", "@xyflow/react"],
          // 차트 라이브러리 (MyPage에서 사용)
          "vendor-charts": ["recharts"],
          // 애니메이션 (전역 사용)
          "vendor-motion": ["framer-motion"],
          // UI 유틸리티
          "vendor-ui": [
            "axios",
            "zustand",
            "lucide-react",
            "react-icons",
            "react-toastify",
          ],
        },
      },
    },
  },

  define: {
    global: "globalThis",
  },

  resolve: {
    // 경로 별칭 (중복 제거됨)
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
    },
  },

  server: {
    host: true,
    port: 5173,

    /**
     * axiosInstance baseURL = "/api/v1"
     * → 따라서 proxy 경로도 "/api/v1" 이어야 한다.
     */
    proxy: {
      "/api/v1": {
        target: "http://localhost:8081",
        changeOrigin: true,
        secure: false,
      },
      // 정적 리소스 (이미지 등) 프록시
      "/uploads": {
        target: "http://localhost:8081",
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
