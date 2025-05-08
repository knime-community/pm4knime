async function createBpmn(xmlString, layouter) {
	const mainContainer = document.createElement("div");
	mainContainer.id = "main";
	mainContainer.style.display = "flex";
	mainContainer.style.flexDirection = "column";
	mainContainer.style.alignItems = "center";
	mainContainer.style.justifyContent = "space-between";
	mainContainer.style.border = "1px solid #ccc";
	mainContainer.style.width = "100vw";
	mainContainer.style.height = "100vh";
	mainContainer.style.background = "white";

	const controlBar = document.createElement("div");
	controlBar.style.background = "#e0e0e0";
	controlBar.style.color = "#fff";
	controlBar.style.padding = "5px";
	controlBar.style.width = "100%";
	controlBar.style.fontFamily = "Arial, sans-serif";

	const controlsDiv = document.createElement("div");
	controlsDiv.id = "zoom-controls";

	const zoomInButton = document.createElement("button");
	zoomInButton.className = "zoom-button";
	zoomInButton.id = "zoom-in";
	const iconZoomIn = document.createElement("i");
	iconZoomIn.className = "fa-solid fa-magnifying-glass-plus";
	zoomInButton.appendChild(iconZoomIn);

	const zoomOutButton = document.createElement("button");
	zoomOutButton.className = "zoom-button";
	zoomOutButton.id = "zoom-out";
	const iconZoomOut = document.createElement("i");
	iconZoomOut.className = "fa-solid fa-magnifying-glass-minus";
	zoomOutButton.appendChild(iconZoomOut);

	const resetButton = document.createElement("button");
	resetButton.className = "reset-button";
	resetButton.id = "reset-button";
	const iconReset = document.createElement("i");
	iconReset.className = "fa-solid fa-rotate-left";
	resetButton.appendChild(iconReset);

	const zoomToFitButton = document.createElement("button");
	zoomToFitButton.className = "zoom-button";
	zoomToFitButton.id = "zoom-to-fit";
	const iconZoomToFit = document.createElement("i");
	iconZoomToFit.className = "fa-solid fa-arrows-to-circle";
	zoomToFitButton.appendChild(iconZoomToFit);
	
	const downloadSvgButton = document.createElement("button");
	downloadSvgButton.className = "zoom-button";
	downloadSvgButton.id = "download-svg";
	const iconDownload = document.createElement("i");
	iconDownload.className = "fa-solid fa-download";
	downloadSvgButton.appendChild(iconDownload); 
	

	controlsDiv.appendChild(zoomInButton);
	controlsDiv.appendChild(zoomOutButton);
	controlsDiv.appendChild(zoomToFitButton);
	controlsDiv.appendChild(resetButton);
	controlsDiv.appendChild(downloadSvgButton);

	controlBar.appendChild(controlsDiv);
	mainContainer.appendChild(controlBar);

	const bpmnDiv = document.createElement("div");
	bpmnDiv.id = "bpmn-container";
	bpmnDiv.style.width = "100%"; // Fixed width
	bpmnDiv.style.height = `calc(100vh - 55px)`;
	bpmnDiv.style.overflow = "auto"; // Enables scrollbars if content overflows
	bpmnDiv.style.margin = "auto";


	appendHiddenBpmnContent(bpmnDiv);
	mainContainer.appendChild(bpmnDiv);

	document.body.appendChild(mainContainer);
	
	if (layouter) {
		console.error('layouter applied: ', layouter);
		try {
			xmlString = await bpmnLayoutWithDagre(xmlString);
		} catch (err) {
			console.error('err: ', err.stack);
		}
	} else {
		console.error('layouter skipped: ', layouter);		
	}

	

	modelXmlString = xmlString;
	const viewer = new BpmnJS({
		container: bpmnDiv,
	});

	await viewer.importXML(modelXmlString);
	viewer.get("canvas").zoom("fit-viewport", "auto");

	const addZoomListeners = (viewer) => {
		let zoomLevel = 1;

		const zoom = (zoomLevel) => {
			viewer.get("canvas").zoom(zoomLevel);
		};

		zoomInButton.addEventListener("click", () => {
			zoomLevel = zoomLevel + 0.2;
			zoom(zoomLevel);
		});

		zoomOutButton.addEventListener("click", () => {
			zoomLevel = zoomLevel - 0.2;
			zoom(zoomLevel);
		});

		document.getElementById("zoom-to-fit").addEventListener("click", () => {
			viewer.get("canvas").zoom("fit-viewport", "auto");
			const currentZoom = viewer.get("canvas").zoom();
			zoomLevel = currentZoom;
		});

		resetButton.addEventListener("click", async () => {
		    try {
		        await viewer.importXML(modelXmlString); 
		        viewer.get('canvas').zoom('fit-viewport', 'auto'); 
		        const currentZoom = viewer.get('canvas').zoom(); 
		        zoomLevel = currentZoom;
		    } catch (err) {
		        console.error('Failed to reset zoom:', err);
		    }
		});

		// Mouse wheel listener for zooming
		mainContainer.addEventListener("wheel", (event) => {
			event.preventDefault(); // Prevent scrolling
			const delta = event.deltaY;
			if (delta > 0) {
				// Zoom out
				zoomLevel = zoomLevel - 0.2;
			} else if (delta < 0) {
				// Zoom in
				zoomLevel = zoomLevel + 0.2;
			}
			zoom(zoomLevel);
		});
		
		downloadSvgButton.addEventListener("click", async () => {
		    try {
		        const { svg } = await viewer.saveSVG(); 
		        const blob = new Blob([svg], { type: 'image/svg+xml' });
		        const url = URL.createObjectURL(blob);
		        const a = document.createElement('a');
		        a.href = url;
		        a.download = 'diagram.svg'; 
		        document.body.appendChild(a);
		        a.click(); 
		        document.body.removeChild(a); 
		        URL.revokeObjectURL(url); 
		    } catch (err) {
		        console.error('Failed to save SVG:', err);
		    }
		});
	};

	addZoomListeners(viewer)

}

function appendHiddenBpmnContent(mainContainer) {
	const hiddenDiv1 = document.createElement('div');
	hiddenDiv1.style.visibility = 'hidden';
	hiddenDiv1.style.position = 'absolute';

	const svg1 = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	/*svg1.setAttribute('width', '90%');
	svg1.setAttribute('height', '90%');*/

	const g = document.createElement('g');
	svg1.appendChild(g);
	hiddenDiv1.appendChild(svg1);

	// Create the canvas div
	const canvasDiv = document.createElement('div');
	canvasDiv.id = 'canvas';
	/*canvasDiv.style.height = '90%';
	canvasDiv.style.width = '90%';*/
	canvasDiv.style.padding = '0';
	canvasDiv.style.margin = '0';
	canvasDiv.style.position = 'absolute';

	// Create the second hidden div
	const hiddenDiv2 = document.createElement('div');
	hiddenDiv2.style.visibility = 'hidden';

	const fixedDiv = document.createElement('div');
	fixedDiv.style.position = 'fixed';
	fixedDiv.style.top = '0px';
	fixedDiv.style.left = '0px';

	const svg2 = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	svg2.id = 'internalSvg';
	svg2.style.width = '0%';
	svg2.style.height = '0%';
	fixedDiv.appendChild(svg2);
	hiddenDiv2.appendChild(fixedDiv);

	const internalCanvasDiv = document.createElement('div');
	internalCanvasDiv.id = 'internalCanvas';
	internalCanvasDiv.style.height = '0%';
	internalCanvasDiv.style.width = '0%';
	internalCanvasDiv.style.padding = '0';
	internalCanvasDiv.style.margin = '0';
	internalCanvasDiv.style.top = '0px';
	internalCanvasDiv.style.left = '0px';
	internalCanvasDiv.style.position = 'fixed';
	hiddenDiv2.appendChild(internalCanvasDiv);

	// Append the created elements to the mainContainer
	mainContainer.appendChild(hiddenDiv1);
	mainContainer.appendChild(canvasDiv);
	mainContainer.appendChild(hiddenDiv2);
}
