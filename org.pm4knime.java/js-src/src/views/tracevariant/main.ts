import { JsonDataService } from "@knime/ui-extension-service";
import { varExplorer } from "./variantExplorer";
import "./tracevariant.css";

const root = document.getElementById("app");
if (root) {
  root.innerHTML = `
    <h2>Trace Variant Explorer</h2>
    <div id="status">Loading...</div>
    <pre id="debug"></pre>
  `;
}

const statusEl = document.getElementById("status");
const debugEl = document.getElementById("debug");

function setStatus(msg: string) {
  if (statusEl) statusEl.textContent = msg;
}

function showDebug(obj: unknown) {
  if (!debugEl) return;
  debugEl.style.display = "block";
  debugEl.textContent = typeof obj === "string" ? obj : JSON.stringify(obj, null, 2);
}

const execute = async () => {
  try {
    const jsonDataService = await JsonDataService.getInstance();
    const data = await jsonDataService.initialData();

    if (!data) {
      setStatus("No data received");
      return;
    }

    if (root) root.remove();

    varExplorer.init(data as any, null);
    setStatus("Rendered");
  } catch (err) {
    setStatus("Failed to load data");
    showDebug(String(err));
  }
};

execute();
