import * as d3 from "d3";
import * as dagreD3 from "dagre-d3";
import BpmnJS from "bpmn-js/lib/Modeler";

class CustomWaypoint {
  dictio: Record<string, unknown>;
  $name: unknown;
  $model: unknown;
  $type: unknown;
  $attrs: unknown;
  $parent: unknown;
  $descriptor: unknown;
  x: unknown;
  y: unknown;

  constructor() {
    this.dictio = {};
  }

  get(varname: string) {
    return this.dictio[varname];
  }

  set(varname: string, varvalue: unknown) {
    this.dictio[varname] = varvalue;
  }
}

function renderGraph(
  iterativelyReachedNodes: any[],
  nodes: any[],
  edgesDict: Record<string, string>,
  nodesep: number,
  edgesep: number,
  ranksep: number,
  targetDivDagre: string,
  desideredWidth: number | null = null,
  desideredHeight: number | null = null,
) {
  const g = new (dagreD3 as any).graphlib.Graph().setGraph({});

  for (const n of iterativelyReachedNodes) {
    if (!n.$type.toLowerCase().endsWith("flow")) {
      let name = `${n.name}`;
      let isProperName = true;
      if (name.length === 0) {
        name = n.id;
        isProperName = false;
      }
      if (name === "start" || name === "end") {
        isProperName = false;
      }
      if (isProperName && desideredWidth != null && desideredHeight != null) {
        g.setNode(n.id, {
          label: n.name.replaceAll(" ", "\n"),
          width: n.name.length * 6,
          height: desideredHeight,
        });
      } else if (desideredWidth != null && desideredHeight != null) {
        g.setNode(n.id, {
          label: n.name.replaceAll(" ", "\n"),
          width: n.name.length * 6,
          height: Math.min(desideredWidth, desideredHeight) * 0.002,
        });
      } else {
        g.setNode(n.id, {
          label: n.name.replaceAll(" ", "\n"),
        });
      }
    }
  }

  for (const n of nodes) {
    if (n.$type.toLowerCase().endsWith("flow")) {
      const source = n.sourceRef.id;
      const target = n.targetRef.id;
      edgesDict[`${source}@${target}`] = n.id;
      g.setEdge(source, target, {
        label: "",
      });
    }
  }

  g.graph().rankDir = "LR";
  g.graph().nodesep = nodesep;
  g.graph().edgesep = edgesep;
  g.graph().ranksep = ranksep;

  const render = new (dagreD3 as any).render();
  const svg = (d3 as any).select(`#${targetDivDagre}`);
  const inner = svg.append("g");
  render(inner, g);

  return g;
}

export async function bpmnLayoutWithDagre(xmlString: string) {
  const targetDivFirstBpmn = "internalCanvas";
  const targetDivDagre = "internalSvg";
  const nodesep = 30;
  const edgesep = 30;
  const ranksep = 85;

  const bpmnViewer = new (BpmnJS as any)({
    container: `#${targetDivFirstBpmn}`,
  });

  await bpmnViewer.importXML(xmlString);

  const nodes = bpmnViewer._definitions.rootElements[0].flowElements;
  const graphical = bpmnViewer._definitions.diagrams[0].plane.planeElement;
  const graphicalDict: Record<string, number> = {};
  const edgesDict: Record<string, string> = {};

  let i = 0;
  while (i < graphical.length) {
    graphicalDict[graphical[i].bpmnElement.id] = i;
    i += 1;
  }

  const toVisit: any[] = [];
  const iterativelyReachedNodes: any[] = [];

  for (const n of nodes) {
    if (n.$type.toLowerCase().endsWith("startevent")) {
      toVisit.push(n);
      break;
    }
  }

  while (toVisit.length > 0) {
    const el = toVisit.pop();
    if (!iterativelyReachedNodes.includes(el)) {
      iterativelyReachedNodes.push(el);
    }
    if (el.outgoing != null) {
      for (const out of el.outgoing) {
        if (!iterativelyReachedNodes.includes(out.targetRef)) {
          toVisit.push(out.targetRef);
        }
      }
    }
  }

  let g = renderGraph(
    iterativelyReachedNodes,
    nodes,
    edgesDict,
    nodesep,
    edgesep,
    ranksep,
    targetDivDagre,
  );

  let desideredWidth = 0;
  let desideredHeight = 0;

  for (const nodeId in g._nodes) {
    const node = g._nodes[nodeId];
    const elemStr = node.elem.innerHTML;
    const width = parseInt(elemStr.split('width="')[1].split('"')[0], 10);
    const height = parseInt(elemStr.split('height="')[1].split('"')[0], 10);
    desideredWidth = Math.max(desideredWidth, width);
    desideredHeight = Math.max(desideredHeight, height);
  }

  g = renderGraph(
    iterativelyReachedNodes,
    nodes,
    edgesDict,
    nodesep,
    edgesep,
    ranksep,
    targetDivDagre,
    desideredWidth * 2.2,
    desideredHeight * 0.7,
  );

  for (const nodeId in g._nodes) {
    const node = g._nodes[nodeId];
    const elemStr = node.elem.innerHTML;
    const width = parseInt(elemStr.split('width="')[1].split('"')[0], 10);
    const height = parseInt(elemStr.split('height="')[1].split('"')[0], 10);
    graphical[graphicalDict[nodeId]].bounds.x = node.x - width / 2.0;
    graphical[graphicalDict[nodeId]].bounds.y = node.y - height / 2.0;
    graphical[graphicalDict[nodeId]].bounds.height = height;
    graphical[graphicalDict[nodeId]].bounds.width = width;
  }

  for (const edgeId in g._edgeLabels) {
    let graphEdgeObj = g._edgeObjs[edgeId];
    graphEdgeObj = `${graphEdgeObj.v}@${graphEdgeObj.w}`;
    const edge = g._edgeLabels[edgeId];
    const graphicalElement = graphical[graphicalDict[edgesDict[graphEdgeObj]]];
    const referenceWaypoint = graphicalElement.waypoint[0];
    graphicalElement.waypoint = [];
    for (const p of edge.points) {
      const waypoint = new CustomWaypoint();
      waypoint.$type = referenceWaypoint.$type;
      waypoint.x = p.x;
      waypoint.y = p.y;
      waypoint.$parent = referenceWaypoint.$parent;
      waypoint.$attrs = referenceWaypoint.$attrs;
      waypoint.$descriptor = referenceWaypoint.$descriptor;
      waypoint.$model = referenceWaypoint.$model;
      waypoint.set("x", p.x);
      waypoint.set("y", p.y);
      graphicalElement.waypoint.push(waypoint);
    }
  }

  const xmlContent = await bpmnViewer.saveXML();
  return xmlContent.xml;
}
