import { JsonDataService } from "@knime/ui-extension-service";
import "./bpmn.css";
import { renderBpmnView } from "./renderBpmn";

type BpmnInitialData = {
  kind: "bpmn";
  xml: string;
  layouter: boolean;
};

async function bootstrap() {
  const app = document.getElementById("app");
  const service = await JsonDataService.getInstance();
  const data = (await service.initialData()) as BpmnInitialData | null;

  if (!data?.xml || !app) {
    if (app) {
      app.textContent = "BPMN viewer could not be initialized.";
    }
    return;
  }

  await renderBpmnView(app, data.xml, Boolean(data.layouter));
}

bootstrap().catch((error) => {
  const app = document.getElementById("app");
  if (app) {
    app.textContent = String(error);
  }
});
