import BpmnJS from "bpmn-js/lib/Modeler";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css";
import "@fortawesome/fontawesome-free/css/all.min.css";
import { bpmnLayoutWithDagre } from "./bpmnLayouter";

type BpmnControls = {
  mainContainer: HTMLDivElement;
  bpmnDiv: HTMLDivElement;
  zoomInButton: HTMLButtonElement;
  zoomOutButton: HTMLButtonElement;
  resetButton: HTMLButtonElement;
  zoomToFitButton: HTMLButtonElement;
  downloadSvgButton: HTMLButtonElement;
};

export async function renderBpmnView(root: HTMLElement, xmlString: string, layouter: boolean) {
  const controls = createBpmnElements(root);
  appendHiddenBpmnContent(controls.bpmnDiv);

  let modelXmlString = xmlString;

  if (layouter) {
    try {
      modelXmlString = await bpmnLayoutWithDagre(xmlString);
    } catch (err: any) {
      console.error("Failed to layout BPMN:", err?.stack ?? err);
    }
  }

  const viewer = new (BpmnJS as any)({
    container: controls.bpmnDiv,
  });

  await viewer.importXML(modelXmlString);
  viewer.get("canvas").zoom("fit-viewport", "auto");

  addZoomListeners(viewer, modelXmlString, controls);
}

function createBpmnElements(root: HTMLElement): BpmnControls {
  root.replaceChildren();

  const mainContainer = document.createElement("div");
  mainContainer.id = "main";

  const controlBar = document.createElement("div");
  controlBar.id = "bpmn-control-bar";

  const controlsDiv = document.createElement("div");
  controlsDiv.id = "zoom-controls";

  const zoomInButton = document.createElement("button");
  zoomInButton.className = "zoom-button";
  zoomInButton.id = "zoom-in";
  zoomInButton.innerHTML = `<i class="fa-solid fa-magnifying-glass-plus"></i>`;

  const zoomOutButton = document.createElement("button");
  zoomOutButton.className = "zoom-button";
  zoomOutButton.id = "zoom-out";
  zoomOutButton.innerHTML = `<i class="fa-solid fa-magnifying-glass-minus"></i>`;

  const resetButton = document.createElement("button");
  resetButton.className = "reset-button";
  resetButton.id = "reset-button";
  resetButton.innerHTML = `<i class="fa-solid fa-rotate-left"></i>`;

  const zoomToFitButton = document.createElement("button");
  zoomToFitButton.className = "zoom-button";
  zoomToFitButton.id = "zoom-to-fit";
  zoomToFitButton.innerHTML = `<i class="fa-solid fa-arrows-to-circle"></i>`;

  const downloadSvgButton = document.createElement("button");
  downloadSvgButton.className = "zoom-button";
  downloadSvgButton.id = "download-svg";
  downloadSvgButton.innerHTML = `<i class="fa-solid fa-download"></i>`;

  controlsDiv.appendChild(zoomInButton);
  controlsDiv.appendChild(zoomOutButton);
  controlsDiv.appendChild(zoomToFitButton);
  controlsDiv.appendChild(resetButton);
  controlsDiv.appendChild(downloadSvgButton);

  controlBar.appendChild(controlsDiv);
  mainContainer.appendChild(controlBar);

  const bpmnDiv = document.createElement("div");
  bpmnDiv.id = "bpmn-container";
  mainContainer.appendChild(bpmnDiv);

  root.appendChild(mainContainer);

  return {
    mainContainer,
    bpmnDiv,
    zoomInButton,
    zoomOutButton,
    resetButton,
    zoomToFitButton,
    downloadSvgButton,
  };
}

function addZoomListeners(viewer: any, modelXmlString: string, controls: BpmnControls) {
  let zoomLevel = 1;

  const zoom = (nextZoomLevel: number) => {
    viewer.get("canvas").zoom(nextZoomLevel);
  };

  controls.zoomInButton.addEventListener("click", () => {
    zoomLevel += 0.2;
    zoom(zoomLevel);
  });

  controls.zoomOutButton.addEventListener("click", () => {
    zoomLevel -= 0.2;
    zoom(zoomLevel);
  });

  controls.zoomToFitButton.addEventListener("click", () => {
    viewer.get("canvas").zoom("fit-viewport", "auto");
    const currentZoom = viewer.get("canvas").zoom();
    zoomLevel = currentZoom;
  });

  controls.resetButton.addEventListener("click", async () => {
    try {
      await viewer.importXML(modelXmlString);
      viewer.get("canvas").zoom("fit-viewport", "auto");
      const currentZoom = viewer.get("canvas").zoom();
      zoomLevel = currentZoom;
    } catch (err) {
      console.error("Failed to reset zoom:", err);
    }
  });

  controls.mainContainer.addEventListener("wheel", (event) => {
    event.preventDefault();
    const delta = event.deltaY;
    if (delta > 0) {
      zoomLevel -= 0.2;
    } else if (delta < 0) {
      zoomLevel += 0.2;
    }
    zoom(zoomLevel);
  });

  controls.downloadSvgButton.addEventListener("click", async () => {
    try {
      const { svg } = await viewer.saveSVG();
      const blob = new Blob([svg], { type: "image/svg+xml" });
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "diagram.svg";
      anchor.click();
      window.setTimeout(() => {
        URL.revokeObjectURL(url);
      }, 60_000);
    } catch (err) {
      console.error("Failed to save SVG:", err);
    }
  });
}

function appendHiddenBpmnContent(mainContainer: HTMLDivElement) {
  const hiddenDiv1 = document.createElement("div");
  hiddenDiv1.style.visibility = "hidden";
  hiddenDiv1.style.position = "absolute";

  const svg1 = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
  svg1.appendChild(g);
  hiddenDiv1.appendChild(svg1);

  const canvasDiv = document.createElement("div");
  canvasDiv.id = "canvas";
  canvasDiv.style.padding = "0";
  canvasDiv.style.margin = "0";
  canvasDiv.style.position = "absolute";

  const hiddenDiv2 = document.createElement("div");
  hiddenDiv2.style.visibility = "hidden";

  const fixedDiv = document.createElement("div");
  fixedDiv.style.position = "fixed";
  fixedDiv.style.top = "0px";
  fixedDiv.style.left = "0px";

  const svg2 = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg2.id = "internalSvg";
  svg2.style.width = "0%";
  svg2.style.height = "0%";
  fixedDiv.appendChild(svg2);
  hiddenDiv2.appendChild(fixedDiv);

  const internalCanvasDiv = document.createElement("div");
  internalCanvasDiv.id = "internalCanvas";
  internalCanvasDiv.style.height = "0%";
  internalCanvasDiv.style.width = "0%";
  internalCanvasDiv.style.padding = "0";
  internalCanvasDiv.style.margin = "0";
  internalCanvasDiv.style.top = "0px";
  internalCanvasDiv.style.left = "0px";
  internalCanvasDiv.style.position = "fixed";
  hiddenDiv2.appendChild(internalCanvasDiv);

  mainContainer.appendChild(hiddenDiv1);
  mainContainer.appendChild(canvasDiv);
  mainContainer.appendChild(hiddenDiv2);
}
