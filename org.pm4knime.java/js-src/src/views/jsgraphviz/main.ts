import { JsonDataService } from "@knime/ui-extension-service";
import "./graph.css";
import { renderGraph, type GraphPayload } from "./renderGraph";

type GraphInitialData = {
  kind: "graph";
  graph: GraphPayload;
};

const app = document.getElementById("app");

if (app) {
  app.innerHTML = `
    <div class="graph-shell">
      <div class="graph-toolbar">
        <div>
          <h2>Process Model View</h2>
          <p id="graph-subtitle">Loading graph data...</p>
        </div>
        <div class="graph-actions">
          <button id="zoom-out" type="button">-</button>
          <button id="zoom-reset" type="button">100%</button>
          <button id="zoom-in" type="button">+</button>
          <button id="download-svg" type="button">Download SVG</button>
        </div>
      </div>
      <div class="graph-stage">
        <div class="graph-card" id="graph-card"></div>
      </div>
    </div>
  `;
}

const subtitle = document.getElementById("graph-subtitle");
const card = document.getElementById("graph-card");
const zoomOut = document.getElementById("zoom-out") as HTMLButtonElement | null;
const zoomReset = document.getElementById("zoom-reset") as HTMLButtonElement | null;
const zoomIn = document.getElementById("zoom-in") as HTMLButtonElement | null;
const downloadButton = document.getElementById("download-svg") as HTMLButtonElement | null;

let scale = 1;
let currentSvg: SVGSVGElement | null = null;

function updateScale(nextScale: number) {
  scale = Math.max(0.4, Math.min(2.5, nextScale));
  if (card) {
    card.style.transform = `scale(${scale})`;
  }
  if (zoomReset) {
    zoomReset.textContent = `${Math.round(scale * 100)}%`;
  }
}

zoomOut?.addEventListener("click", () => updateScale(scale - 0.1));
zoomReset?.addEventListener("click", () => updateScale(1));
zoomIn?.addEventListener("click", () => updateScale(scale + 0.1));

downloadButton?.addEventListener("click", () => {
  if (!currentSvg) {
    return;
  }
  const serialized = new XMLSerializer().serializeToString(currentSvg);
  const blob = new Blob([serialized], { type: "image/svg+xml;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = "graph.svg";
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
});

async function bootstrap() {
  const service = await JsonDataService.getInstance();
  const data = (await service.initialData()) as GraphInitialData | null;

  if (!card || !subtitle || !data?.graph) {
    if (card) {
      card.innerHTML = `<div class="graph-empty">No graph data received.</div>`;
    }
    return;
  }

  subtitle.textContent = `${data.graph.nodes?.length ?? 0} nodes, ${data.graph.links?.length ?? 0} links`;

  currentSvg = renderGraph(data.graph);
  card.replaceChildren(currentSvg);
  updateScale(1);
}

bootstrap().catch((error) => {
  if (card) {
    card.innerHTML = `<div class="graph-empty">${String(error)}</div>`;
  }
});
