import { defineConfig } from "vite";
import { resolve } from "path";

export default defineConfig({
  base: "./",
  build: {
    rollupOptions: {
      input: {
        tracevariant: resolve(__dirname, "src/views/tracevariant/index.html"),
      },
    },
  },
});
