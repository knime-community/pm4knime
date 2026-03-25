import { JsonDataService } from "@knime/ui-extension-service";

type BpmnInitialData = {
  kind: "bpmn";
  xml: string;
  layouter: boolean;
};

declare global {
  interface Window {
    createBpmn?: (xml: string, layouter: boolean) => Promise<void>;
  }
}

async function bootstrap() {
  const service = await JsonDataService.getInstance();
  const data = (await service.initialData()) as BpmnInitialData | null;

  if (!data?.xml || typeof window.createBpmn !== "function") {
    const app = document.getElementById("app");
    if (app) {
      app.textContent = "BPMN viewer could not be initialized.";
    }
    return;
  }

  await window.createBpmn(data.xml, Boolean(data.layouter));
}

bootstrap().catch((error) => {
  const app = document.getElementById("app");
  if (app) {
    app.textContent = String(error);
  }
});
