import { defineConfig } from "vite";
import { resolve } from "path";

export default defineConfig({
  base: "./",
  build: {
    rollupOptions: {
      input: {
        bpmn: resolve(__dirname, "src/views/bpmn/index.html"),
        jsgraphviz: resolve(__dirname, "src/views/jsgraphviz/index.html"),
        tracevariant: resolve(__dirname, "src/views/tracevariant/index.html"),
      },
    },
  },
});
