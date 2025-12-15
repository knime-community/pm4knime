let initialGraphState;
let padding_inside_paper = 10;

function createGraphElements() {

	const mainContainer = document.createElement("div");
	mainContainer.id = "main";
	mainContainer.style.display = "flex";
	mainContainer.style.flexDirection = "column"; // Stack children vertically
	mainContainer.style.alignItems = "center"; // Center-align children horizontally
	mainContainer.style.justifyContent = "center";
	// mainContainer.style.background = "#f0ede6";
	mainContainer.style.flexDirection = "column";
	mainContainer.style.border = "1px solid #ccc";
	mainContainer.style.width = "100%"; // Full width of the viewport
	mainContainer.style.height = "50%"; // Full height of the viewport

	const controlBar = document.createElement("div");
	controlBar.style.background = "#e0e0e0";
	controlBar.style.color = "#fff";
	controlBar.style.width = "100%"; // Fixed width
	//controlBar.style.height = "35px";
	// controlBar.textContent = "Control Bar";
	controlBar.style.fontFamily = "Arial, sans-serif";

	// Create the graph container with fixed size
	const graphContainer = document.createElement("div");
	graphContainer.id = "graphContainer";
	graphContainer.style.width = "100%"; // Fixed width

	const controlBarHeight = controlBar.offsetHeight; // Get the dynamic height of the controlBar
	graphContainer.style.height = `calc(100vh - 55px)`;

	// graphContainer.style.border = "1px solid #ccc";
	mainContainer.style.background = "white";
	graphContainer.style.overflow = "auto"; // Enables scrollbars if content overflows
	graphContainer.style.margin = "auto";


	const paperDiv = document.createElement("div");
	paperDiv.id = "paper";
	paperDiv.style.width = "100%"; // Fixed width
	paperDiv.style.height = "100%";
	paperDiv.style.margin = "auto";

	// Create controls div
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

	graphContainer.appendChild(paperDiv);

	mainContainer.appendChild(controlBar);
	mainContainer.appendChild(graphContainer);
	document.body.appendChild(mainContainer);

};


function estimateTextWidth(text, fontSize) {
	if (typeof text === "undefined" || text === "") {
		return 0; // Return a default width or 0 if there's no text to measure
	}
	const averageCharWidth = fontSize * 0.7; // Adjust based on your font
	return text.length * averageCharWidth;
}

function adjustPaperSize(graph, paper) {
	let maxX = 0;
	let maxY = 0;

	graph.getElements().forEach((element) => {
		let position = element.position();
		let size = element.size();
		maxX = Math.max(maxX, position.x + size.width);
		maxY = Math.max(maxY, position.y + size.height);
	});

	paper.setDimensions(maxX + 100, maxY + 100);

	paper.fitToContent({
		useModelGeometry: true,
		padding: padding_inside_paper,
		allowNewOrigin: "any",
	});
}

// This function will check each set of three consecutive points to determine if they are collinear. If they are, the middle point can be removed. First and last points are always skipped.
function simplifyWaypoints(points) {
	let simplified = [];

	for (let i = 1; i < points.length - 1; i++) {
		let prev = points[i - 1];
		let curr = points[i];
		let next = points[i + 1];

		// Calculate the determinant of the matrix formed by three points
		// | 1  x1  y1 |
		// | 1  x2  y2 |  = 0 for collinear points
		// | 1  x3  y3 |
		// If determinant is zero, points are collinear

		let det =
			prev.x * (curr.y - next.y) +
			curr.x * (next.y - prev.y) +
			next.x * (prev.y - curr.y);

		if (Math.abs(det) > 1e-10) {
			// Use a small epsilon to handle floating point errors
			simplified.push(curr); // Keep current point if it's not collinear
		}
	}

	return simplified;
}



function createPaper(nodes, edges) {

	var graph = new joint.dia.Graph();

	var paper = new joint.dia.Paper({
		el: document.getElementById("paper"),
		width: "100%", // Fixed width
		height: "100%",
		// gridSize: 10,
		defaultAnchor: { name: "perpendicular" },
		defaultConnectionPoint: { name: "boundary" },
		model: graph,
	});

	const zoom = (zoomLevel) => {
		paper.scale(zoomLevel);
		paper.fitToContent({
			useModelGeometry: true,
			padding: padding_inside_paper,
			allowNewOrigin: "any",
		});
	};

	var pn = joint.shapes.pn;

	var elements = {};

	// Add nodes
	nodes.forEach(function(node) {

		if (node.type === "activity")
			tb_flag = 1;

		if (node.type === "operator")
			process_tree_flag = 1;

		var element;
		if (node.type === "place") {
			let attrs = {
				".root": { stroke: "grey", "stroke-width": 2 },
				".tokens > circle": { fill: "#38761d" },
			};
			let tokens = 0;

			if (node.final === true) {
				attrs[".root"]["stroke-width"] = 4;
			}
			if (node.initial === true) {
				tokens = 1;
			}

			node.width = 50;
			node.height = 50;

			element = new pn.Place({
				position: node.position,
				attrs: attrs,
				tokens: tokens,
				size: { width: 50, height: 50 },
			});
		}
		else if (node.type === "activity") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;
			element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#e0e0e0",
						stroke: "#999999",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "transition") {
			const fontSize = 14; // the font size of the labels must be set in the css file
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;
			element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#cfe2f3",
						stroke: "#3f77cf",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "artificial start") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;

			var element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#c8fcc0",
						stroke: "#167f06",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "artificial end") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;

			var element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#fcb6b6",
						stroke: "#c30909",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "manual") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;

			var element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#e0e0e0",
						stroke: "#999999",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "automatic") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;

			var element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: node.label || "",
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#e0e0e0",
						stroke: "#999999",
						"stroke-width": 2,
					},

				},

			});
		}
		else if (node.type === "operator") {
			const fontSize = 14;
			const textWidth = estimateTextWidth(node.label, fontSize);
			const transitionWidth = Math.max(textWidth + 10, 20);
			node.width = transitionWidth;
			node.height = 35;
			var operatorSymbol = "";

			if (node.label === "xlp") {
				operatorSymbol = "⭯";
			}

			else if (node.label === "xor") {
				operatorSymbol = "✖";
			}

			else if (node.label === "and") {
				operatorSymbol = "✙";
			}

			else if (node.label === "seq") {
				operatorSymbol = "➜";
			}

			var element = new pn.Transition({
				position: node.position,
				size: { width: transitionWidth, height: 50 },
				attrs: {
					".label": {
						text: operatorSymbol,
						"fill": "black",
						"ref-x": 0.5,
						"ref-y": 0.5,
						"text-anchor": "middle",
						"y-alignment": "middle",
					},
					".root": {
						fill: "#add8e6",
						stroke: "#87ceeb",
						"stroke-width": 2,
					},

				},

			});
		}

		graph.addCell(element);

		elements[node.id] = element;

	});

	edges.forEach(function(edge) {

		var linkAttrs = {
			".connection": { stroke: "grey", "stroke-width": 3 },
			".marker-target": { fill: "grey", stroke: "grey", "stroke-width": 2 }
		};
		var labels = [];
		if (edge.frequency !== undefined && edge.frequency !== null) {
			labels.push({
				position: 0.5,
				attrs: {
					text: { text: edge.frequency.toString(), fill: 'black', 'font-size': 12 },
					rect: { fill: 'white', stroke: 'none' }
				}
			});
		}

		var link = new pn.Link({
			source: { id: elements[edge.source].id, selector: ".root" },
			target: { id: elements[edge.target].id, selector: ".root" },
			attrs: linkAttrs,
			labels: labels,
		});

		if (edge.frequency > 0) {
			link.attr('.connection', { stroke: '#000f80' });
			if (process_tree_flag === 1)
				link.attr('.marker-target', { fill: 'none', stroke: 'none' });
			else
				link.attr('.marker-target', { fill: '#000f80', stroke: '#000f80' });
			tb_flag = 1;
		}
		else if (edge.frequency === 0 || edge.frequency < -1) { // xor, and, redo
			link.attr('.connection', { stroke: '#000f80' });
			link.attr('.marker-target', { fill: 'none', stroke: 'none' });
			link.label(0, { attrs: { text: { text: '' } } });
			tb_flag = 1;
		}
		else if (edge.frequency === -1) {
			link.attr('.connection', { stroke: '#000f80' });
			link.attr('.marker-target', { fill: 'none', stroke: 'none' });
			link.label(0, { attrs: { text: { text: 'do' } } });
			tb_flag = 1;
		}

		if (edge.type === 'SureEdge' || edge.type === 'HybridDirectedSureGraphEdge') {
			link.attr('.connection', { stroke: '#000f80' });
			link.attr('.marker-target', { fill: '#000f80', stroke: '#000f80' });
		}
		else if (edge.type === 'UncertainEdge' || edge.type === 'HybridDirectedUncertainGraphEdge') {
			link.attr('.connection', { stroke: 'red', 'stroke-dasharray': '4,2' });
			link.attr('.marker-target', { fill: 'red', stroke: 'red' });
		}
		else if (edge.type === 'LongDepEdge' || edge.type === 'HybridDirectedLongDepGraphEdge') {
			link.attr('.connection', { stroke: 'orange' });
			link.attr('.marker-target', { fill: 'orange', stroke: 'orange' });
		}

		graph.addCell(link);
	});

	applyAutoLayout(nodes, edges, elements);

	adjustPaperSize(graph, paper);

	initialGraphState = JSON.parse(JSON.stringify(graph.toJSON()));
	let bbox = paper.getContentBBox();
	let graphWidth = bbox.width;
	let graphHeight = bbox.height;



	const addZoomListeners = (paper) => {
		let zoomLevel = 1;

		const zoom = (zoomLevel) => {
			paper.scale(zoomLevel);
			paper.fitToContent({
				useModelGeometry: true,
				padding: padding_inside_paper,
				allowNewOrigin: "any",
			});
		};


		document.getElementById("zoom-in").addEventListener("click", () => {
			zoomLevel = zoomLevel + 0.2;
			zoom(zoomLevel);
		});

		document.getElementById("zoom-out").addEventListener("click", () => {
			zoomLevel = zoomLevel - 0.2;
			zoom(zoomLevel);
		});

		document.getElementById("zoom-to-fit").addEventListener("click", () => {

			let graphContainer = document.getElementById('graphContainer');
			let containerWidth = graphContainer.getBoundingClientRect().width - (3 * padding_inside_paper);
			let containerHeight = graphContainer.getBoundingClientRect().height - (3 * padding_inside_paper);


			let paperContainer = document.getElementById('paper');
			let paperWidth = paper.getContentBBox().width;
			let paperHeight = paper.getContentBBox().height;

			let scaleX = containerWidth / paperWidth;
			let scaleY = containerHeight / paperHeight;

			zoomLevel = zoomLevel * Math.min(scaleX, scaleY);
			zoom(zoomLevel);


		});

		document.getElementById("reset-button").addEventListener("click", () => {

			var graph = paper.model;
			graph.clear();
			graph.fromJSON(initialGraphState); // Restore the graph from the initial saved state

			zoomLevel = 1;
			zoom(zoomLevel);
		});

		paper.el.addEventListener("wheel", (event) => {
			event.preventDefault(); // Prevent scrolling
			const delta = event.deltaY;
			// Determine zoom direction
			if (delta > 0) {
				// Zoom out
				zoomLevel = zoomLevel - 0.2;
			} else if (delta < 0) {
				// Zoom in
				zoomLevel = zoomLevel + 0.2;
			}
			zoom(zoomLevel);
		});

		paper.on("element:pointerup link:pointerup", (cellView) => {
			paper.fitToContent({
				useModelGeometry: true,
				padding: padding_inside_paper,
				allowNewOrigin: "any",
			});
		});

		document.getElementById("download-svg").addEventListener("click", async () => {

			const serializedSVG = createSVG(paper);
			const blob = new Blob([serializedSVG], { type: 'image/svg+xml' });
			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = 'graph.svg';
			document.body.appendChild(a);
			a.click();
			document.body.removeChild(a);
			URL.revokeObjectURL(url);
		});
	};

	addZoomListeners(paper);

	function applyAutoLayout() {

		var g = new dagre.graphlib.Graph();

		if (tb_flag == 0) {
			g.setGraph({
				rankdir: "LR",
				marginx: 20,
				marginy: 20,
			});
		}
		else { //for causal graphs and DFGs
			g.setGraph({
				rankdir: "TB",
				marginx: 20,
				marginy: 20,
			});
		}

		g.setDefaultEdgeLabel(function() {
			return {};
		});

		nodes.forEach((node) => {
			// const fontSize = 14;
			// const textWidth = estimateTextWidth(node.label, fontSize);
			// const transitionWidth = Math.max(textWidth + 10, 20);
			g.setNode(node.id, {
				width: node.width,
				height: node.height,
				label: node.label,
			});
		});

		edges.forEach((edge) => {
			g.setEdge(edge.source, edge.target, {
				label: edge.label,
				width: 0,
				height: 0,
			});
		});

		if (process_tree_flag === 1)
			dagre.layout(g, { disableOrder: true });
		else
			dagre.layout(g);

		g.nodes().forEach(function(v) {
			let node = g.node(v);
			elements[v].position(node.x - node.width / 2, node.y - node.height / 2);
			elements[v].resize(node.width, node.height);

		});

		g.edges().forEach(function(e) {
			let edge = g.edge(e);
			let sourceElement = elements[e.v];
			let targetElement = elements[e.w];

			if (sourceElement && targetElement) {
				let links = graph
					.getConnectedLinks(sourceElement, { outbound: true })
					.filter((link) => link.getTargetElement() === targetElement);

				if (links.length > 0) {
					let link = links[0];
					let waypoints = edge.points.map((point) => ({
						x: point.x,
						y: point.y,
					}));
					let simplifiedWaypoints = simplifyWaypoints(waypoints); // Simplify waypoints
					link.vertices(simplifiedWaypoints); // Update the vertices with the simplified waypoints
				} else {
					console.error("No link found between nodes", e.v, "and", e.w);
				}
			} else {
				console.error(
					"Source or target elements not found for nodes",
					e.v,
					"and",
					e.w
				);
			}
		});

		zoomLevel = 1
		zoom(zoomLevel);
	}

	return paper;
};



function createSVG(paper) {
	const svgElement = paper.svg.cloneNode(true);
	svgElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
	const bbox = paper.getContentBBox();
	const width_with_padding = bbox.width + 2*padding_inside_paper;
	const height_with_padding = bbox.height + 2*padding_inside_paper;

	svgElement.setAttribute("width", width_with_padding);
	svgElement.setAttribute("height", height_with_padding);
	svgElement.setAttribute("viewBox", `${bbox.x} ${bbox.y} ${width_with_padding} ${height_with_padding}`);

	// Create a style element for the SVG
	const cssStyle = document.createElement('style');
	cssStyle.setAttribute('type', 'text/css');

	cssStyle.textContent = graphCSSText;
	svgElement.prepend(cssStyle); // Prepend the style element to the SVG

	return (new XMLSerializer()).serializeToString(svgElement);
}