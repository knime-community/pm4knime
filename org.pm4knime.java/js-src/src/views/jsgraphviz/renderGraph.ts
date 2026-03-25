export type GraphNode = {
  id: string;
  type?: string;
  label?: string;
  initial?: boolean;
  final?: boolean;
  i_marking?: boolean;
  f_marking?: boolean;
};

export type GraphEdge = {
  source: string;
  target: string;
  type?: string;
  frequency?: number | string;
};

export type GraphPayload = {
  nodes?: GraphNode[];
  links?: GraphEdge[];
};

type PositionedNode = {
  node: GraphNode;
  x: number;
  y: number;
  width: number;
  height: number;
};

const MARGIN = 36;
const RANK_SPACING = 180;
const LANE_SPACING = 120;

export function renderGraph(graph: GraphPayload): SVGSVGElement {
  const nodes = graph.nodes ?? [];
  const links = graph.links ?? [];

  if (!nodes.length) {
    return createMessageSvg("No graph data available.");
  }

  const topToBottom = nodes.some((node) =>
    ["activity", "artificial start", "artificial end"].includes((node.type ?? "").toLowerCase()),
  );
  const processTree = nodes.some((node) =>
    ["operator", "manual", "automatic"].includes((node.type ?? "").toLowerCase()),
  );

  const positioned = layout(nodes, links, topToBottom);
  const width = Math.max(...positioned.map((node) => node.x + node.width)) + MARGIN;
  const height = Math.max(...positioned.map((node) => node.y + node.height)) + MARGIN;

  const svg = createSvg(width, height);
  svg.appendChild(rect(0, 0, width, height, "#f8fafc"));

  const positionsById = new Map(positioned.map((node) => [node.node.id, node]));

  for (const edge of links) {
    const source = positionsById.get(edge.source);
    const target = positionsById.get(edge.target);
    if (!source || !target) {
      continue;
    }

    const x1 = topToBottom ? source.x + source.width / 2 : source.x + source.width;
    const y1 = topToBottom ? source.y + source.height : source.y + source.height / 2;
    const x2 = topToBottom ? target.x + target.width / 2 : target.x;
    const y2 = topToBottom ? target.y : target.y + target.height / 2;

    const type = (edge.type ?? "").toLowerCase();
    const stroke =
      type.includes("uncertainedge") || type.includes("hybriddirecteduncertaingraphedge")
        ? "#dc2626"
        : type.includes("longdepedge") || type.includes("hybriddirectedlongdepgraphedge")
          ? "#d97706"
          : "#334155";

    const lineEl = line(x1, y1, x2, y2, stroke);
    if (stroke === "#dc2626") {
      lineEl.setAttribute("stroke-dasharray", "6 4");
    }
    if (!processTree) {
      lineEl.setAttribute("marker-end", `url(#${markerId(stroke)})`);
    }
    svg.appendChild(lineEl);

    const label = edgeLabel(edge, processTree);
    if (label) {
      const labelX = (x1 + x2) / 2;
      const labelY = (y1 + y2) / 2 - 8;
      const group = create("g");
      group.appendChild(roundedRect(labelX - 16, labelY - 14, 32, 18, 6, "#ffffff"));
      group.appendChild(text(label, labelX, labelY, 11));
      svg.appendChild(group);
    }
  }

  for (const item of positioned) {
    svg.appendChild(renderNode(item));
  }

  return svg;
}

function layout(nodes: GraphNode[], links: GraphEdge[], topToBottom: boolean): PositionedNode[] {
  const outgoing = new Map<string, string[]>();
  const indegree = new Map<string, number>();

  for (const node of nodes) {
    outgoing.set(node.id, []);
    indegree.set(node.id, 0);
  }

  for (const edge of links) {
    if (!outgoing.has(edge.source) || !indegree.has(edge.target)) {
      continue;
    }
    outgoing.get(edge.source)!.push(edge.target);
    indegree.set(edge.target, (indegree.get(edge.target) ?? 0) + 1);
  }

  const ranks = new Map<string, number>();
  const queue: string[] = [];

  [...indegree.entries()]
    .filter(([, degree]) => degree === 0)
    .map(([id]) => id)
    .sort()
    .forEach((id) => {
      ranks.set(id, 0);
      queue.push(id);
    });

  if (!queue.length && nodes.length) {
    const first = [...nodes].sort((a, b) => a.id.localeCompare(b.id))[0];
    ranks.set(first.id, 0);
    queue.push(first.id);
  }

  const indegreeCopy = new Map(indegree);

  while (queue.length) {
    const source = queue.shift()!;
    const sourceRank = ranks.get(source) ?? 0;

    for (const target of outgoing.get(source) ?? []) {
      ranks.set(target, Math.max(ranks.get(target) ?? 0, sourceRank + 1));
      indegreeCopy.set(target, (indegreeCopy.get(target) ?? 0) - 1);
      if ((indegreeCopy.get(target) ?? 0) === 0) {
        queue.push(target);
      }
    }
  }

  let fallbackRank = Math.max(...ranks.values(), 0) + 1;
  for (const node of nodes) {
    if (!ranks.has(node.id)) {
      ranks.set(node.id, fallbackRank++);
    }
  }

  const byRank = new Map<number, GraphNode[]>();
  for (const node of nodes) {
    const rank = ranks.get(node.id) ?? 0;
    const rankNodes = byRank.get(rank) ?? [];
    rankNodes.push(node);
    byRank.set(rank, rankNodes);
  }

  for (const rankNodes of byRank.values()) {
    rankNodes.sort((a, b) =>
      `${a.type ?? ""}:${a.label ?? ""}:${a.id}`.localeCompare(`${b.type ?? ""}:${b.label ?? ""}:${b.id}`),
    );
  }

  const result: PositionedNode[] = [];
  for (const [rank, rankNodes] of [...byRank.entries()].sort((a, b) => a[0] - b[0])) {
    rankNodes.forEach((node, lane) => {
      const width = estimateWidth(node);
      const height = estimateHeight(node);
      const primary = MARGIN + rank * RANK_SPACING;
      const secondary = MARGIN + lane * LANE_SPACING;
      result.push({
        node,
        x: topToBottom ? secondary : primary,
        y: topToBottom ? primary : secondary,
        width,
        height,
      });
    });
  }

  return result;
}

function renderNode(item: PositionedNode): SVGGElement {
  const group = create("g");
  const node = item.node;
  const type = (node.type ?? "").toLowerCase();

  if (type === "place") {
    const cx = item.x + item.width / 2;
    const cy = item.y + item.height / 2;
    const radius = item.width / 2 - 2;
    group.appendChild(circle(cx, cy, radius, "#ffffff", "#475569"));
    if (node.final || node.f_marking) {
      group.appendChild(circle(cx, cy, radius - 5, "none", "#475569"));
    }
    if (node.initial || node.i_marking) {
      group.appendChild(circle(cx, cy, 6, "#15803d", "#15803d"));
    }
    return group;
  }

  const { fill, stroke } = nodeColors(type);
  group.appendChild(
    roundedRect(item.x, item.y, item.width, item.height, type === "transition" ? 4 : 12, fill, stroke),
  );

  const label = displayLabel(node);
  if (label) {
    group.appendChild(text(label, item.x + item.width / 2, item.y + item.height / 2 + 5, 13));
  }
  return group;
}

function nodeColors(type: string) {
  if (type === "artificial start") {
    return { fill: "#c8fcc0", stroke: "#167f06" };
  }
  if (type === "artificial end") {
    return { fill: "#fecaca", stroke: "#b91c1c" };
  }
  if (type === "activity") {
    return { fill: "#e2e8f0", stroke: "#64748b" };
  }
  if (type === "operator") {
    return { fill: "#dbeafe", stroke: "#60a5fa" };
  }
  if (type === "transition") {
    return { fill: "#dbeafe", stroke: "#2563eb" };
  }
  return { fill: "#f8fafc", stroke: "#64748b" };
}

function displayLabel(node: GraphNode) {
  if ((node.type ?? "").toLowerCase() !== "operator") {
    return node.label ?? "";
  }
  switch ((node.label ?? "").toLowerCase()) {
    case "xlp":
      return "loop";
    case "xor":
      return "xor";
    case "and":
      return "and";
    case "seq":
      return "seq";
    default:
      return node.label ?? "";
  }
}

function edgeLabel(edge: GraphEdge, processTree: boolean) {
  if (edge.frequency === undefined || edge.frequency === null) {
    return "";
  }
  const value = Number(edge.frequency);
  if (!Number.isNaN(value)) {
    if (processTree) {
      if (value === -1) {
        return "do";
      }
      if (value <= 0) {
        return "";
      }
    }
    return String(Math.trunc(value));
  }
  return String(edge.frequency);
}

function estimateWidth(node: GraphNode) {
  if ((node.type ?? "").toLowerCase() === "place") {
    return 42;
  }
  const labelLength = Math.max(displayLabel(node).length, 1);
  return Math.max(70, 18 + labelLength * 8);
}

function estimateHeight(node: GraphNode) {
  return (node.type ?? "").toLowerCase() === "place" ? 42 : 40;
}

function createMessageSvg(message: string) {
  const svg = createSvg(640, 80);
  svg.appendChild(rect(0, 0, 640, 80, "#f8fafc"));
  svg.appendChild(text(message, 24, 44, 16, "start"));
  return svg;
}

function createSvg(width: number, height: number) {
  const svg = create("svg") as SVGSVGElement;
  svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
  svg.setAttribute("width", String(width));
  svg.setAttribute("height", String(height));
  svg.setAttribute("viewBox", `0 0 ${width} ${height}`);

  const defs = create("defs");
  defs.appendChild(marker("#334155", markerId("#334155")));
  defs.appendChild(marker("#dc2626", markerId("#dc2626")));
  defs.appendChild(marker("#d97706", markerId("#d97706")));
  svg.appendChild(defs);
  return svg;
}

function markerId(color: string) {
  return color === "#dc2626" ? "arrow-red" : color === "#d97706" ? "arrow-orange" : "arrow-blue";
}

function marker(color: string, id: string) {
  const markerEl = create("marker");
  markerEl.setAttribute("id", id);
  markerEl.setAttribute("markerWidth", "10");
  markerEl.setAttribute("markerHeight", "10");
  markerEl.setAttribute("refX", "8");
  markerEl.setAttribute("refY", "3");
  markerEl.setAttribute("orient", "auto");

  const path = create("path");
  path.setAttribute("d", "M0,0 L0,6 L8,3 z");
  path.setAttribute("fill", color);
  markerEl.appendChild(path);
  return markerEl;
}

function roundedRect(
  x: number,
  y: number,
  width: number,
  height: number,
  radius: number,
  fill: string,
  stroke = "none",
) {
  const rectEl = rect(x, y, width, height, fill, stroke);
  rectEl.setAttribute("rx", String(radius));
  return rectEl;
}

function rect(x: number, y: number, width: number, height: number, fill: string, stroke = "none") {
  const rectEl = create("rect");
  rectEl.setAttribute("x", String(x));
  rectEl.setAttribute("y", String(y));
  rectEl.setAttribute("width", String(width));
  rectEl.setAttribute("height", String(height));
  rectEl.setAttribute("fill", fill);
  rectEl.setAttribute("stroke", stroke);
  rectEl.setAttribute("stroke-width", stroke === "none" ? "0" : "2.5");
  return rectEl;
}

function circle(cx: number, cy: number, radius: number, fill: string, stroke: string) {
  const circleEl = create("circle");
  circleEl.setAttribute("cx", String(cx));
  circleEl.setAttribute("cy", String(cy));
  circleEl.setAttribute("r", String(radius));
  circleEl.setAttribute("fill", fill);
  circleEl.setAttribute("stroke", stroke);
  circleEl.setAttribute("stroke-width", stroke === "none" ? "0" : "2.5");
  return circleEl;
}

function line(x1: number, y1: number, x2: number, y2: number, stroke: string) {
  const lineEl = create("line");
  lineEl.setAttribute("x1", String(x1));
  lineEl.setAttribute("y1", String(y1));
  lineEl.setAttribute("x2", String(x2));
  lineEl.setAttribute("y2", String(y2));
  lineEl.setAttribute("stroke", stroke);
  lineEl.setAttribute("stroke-width", "2.5");
  return lineEl;
}

function text(value: string, x: number, y: number, size: number, anchor = "middle") {
  const textEl = create("text");
  textEl.textContent = value;
  textEl.setAttribute("x", String(x));
  textEl.setAttribute("y", String(y));
  textEl.setAttribute("text-anchor", anchor);
  textEl.setAttribute("font-size", String(size));
  textEl.setAttribute("font-family", "Arial, sans-serif");
  textEl.setAttribute("fill", "#0f172a");
  return textEl;
}

function create<K extends keyof SVGElementTagNameMap>(name: K): SVGElementTagNameMap[K] {
  return document.createElementNS("http://www.w3.org/2000/svg", name);
}
