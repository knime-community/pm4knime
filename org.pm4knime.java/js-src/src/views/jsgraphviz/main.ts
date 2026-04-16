import { JsonDataService } from "@knime/ui-extension-service";
import "./graph.css";
import { renderGraphView, type GraphPayload } from "./renderGraph";

type GraphInitialData = {
  kind: "graph";
  graph: GraphPayload;
};

const app = document.getElementById("app");

function ensureLoadingIndicator() {
  let loading = document.getElementById("loading");
  if (loading) {
    return loading;
  }

  loading = document.createElement("div");
  loading.id = "loading";

  const spinner = document.createElement("div");
  spinner.className = "spinner";
  loading.appendChild(spinner);

  document.body.appendChild(loading);
  return loading;
}

function setLoading(visible: boolean) {
  const loading = ensureLoadingIndicator();
  loading.style.display = visible ? "block" : "none";
}

async function bootstrap() {
  if (!app) {
    return;
  }

  setLoading(true);

  try {
    const service = await JsonDataService.getInstance();
    const data = (await service.initialData()) as GraphInitialData | null;

    if (!data?.graph) {
      app.textContent = "No graph data received.";
      return;
    }

    renderGraphView(app, data.graph);
  } catch (error) {
    app.textContent = String(error);
  } finally {
    setLoading(false);
  }
}

bootstrap();
