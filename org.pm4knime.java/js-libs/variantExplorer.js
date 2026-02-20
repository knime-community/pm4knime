var varExplorer = (function () {

    let _representation;
    let _value;

    let _allVariants = [];
    let _renderIndex = 0;

    let _activityColorMap = {};
    let _totalFrequency = 0;
    const _variantSVGCache = new Map();
    let _sortField = "frequency";
	let _sortDirection = "desc";

    const BATCH_SIZE = 5;

    // Track expanded state per visible row index
    const _expandedRows = new Map(); // key: variant index (0-based), value: boolean

    let view = {};

    // =====================================================
    // INIT
    // =====================================================
    view.init = function (representation, value) {

        _representation = representation;
        _value = value;

        _allVariants = representation.variants.variants || [];

        _totalFrequency = _allVariants.length
            ? _allVariants.map(v => v.frequency || 0).reduce((a, b) => a + b, 0)
            : 0;

        _renderIndex = 0;

        // Build activity→color map ONCE
        _activityColorMap = createActivityColorMap(representation.variants.activities || []);

        // Build UI skeleton
        createUI();

        // Render first batch
        renderNextBatch();
    };

    view.getComponentValue = () => _value;

    // =====================================================
    // UI CREATION (only once)
    // =====================================================
    function createUI() {

	    let body = document.body;
	    body.style.margin = "0";
	    body.style.padding = "18px";
	    body.style.background = "#f8fafc";
	    body.style.fontFamily = "Arial, sans-serif";
	    body.style.color = "#0f172a";
	
	    // Add CSS to hide horizontal scrollbar
	    let style = document.createElement("style");
	    style.innerHTML = `
		  .trace-scroll {
		    position: relative;
		    display: inline-block;
		  }
		
		  .trace-scroll::-webkit-scrollbar {
		    display: none;
		  }
		
		  .trace-scroll:after {
		    content: "";
		    position: absolute;
		    top: 0;
		    right: 0;
		    width: 40px;
		    height: 100%;
		    background: linear-gradient(to right, rgba(255,255,255,0), white);
		    pointer-events: none;
		  }
		`;
	    document.head.appendChild(style);
	
	    // ---- Title ----
	    let title = document.createElement("div");
	    title.innerText = "Trace Variants Explorer";
	    title.style.cssText =
	        "font-size:18px;font-weight:700;margin-bottom:8px;";
	    body.appendChild(title);
	
	    
	    // ---- Toolbar Row ----
		let toolbar = document.createElement("div");
		toolbar.style.cssText = `
		  display:flex;
		  justify-content:space-between;
		  align-items:center;
		  padding:6px 10px;
		  border-radius:10px;
		  background:#f1f5f9;
		  font-size:13px;
		  color:#475569;
		  margin-bottom:14px;
		`;
		
		let leftInfo = document.createElement("div");
		leftInfo.id = "toolbar-info";
		leftInfo.innerText = "Order variants";
		
		let rightControls = document.createElement("div");
		rightControls.style.cssText = `
		  display:flex;
		  align-items:center;
		  gap:6px;
		`;
		
		let orderLabel = document.createElement("span");
		orderLabel.innerText = "Order:";
		orderLabel.style.fontSize = "12px";
		orderLabel.style.color = "#64748b";
		
		let sortSelect = document.createElement("select");
		sortSelect.id = "sort-select";
		sortSelect.style.cssText = `
		  font-size:12px;
		  padding:2px 6px;
		  border-radius:6px;
		  border:1px solid rgba(15,23,42,0.12);
		  background:#f8fafc;
		  color:#0f172a;
		  cursor:pointer;
		  outline:none;
		`;
		
		sortSelect.innerHTML = `
		  <option value="frequency-desc">frequency ↓</option>
		  <option value="frequency-asc">frequency ↑</option>
		  <option value="length-desc">length ↓</option>
		  <option value="length-asc">length ↑</option>
		`;
		
		sortSelect.onchange = function () {
		  applySorting(this.value);
		};
		
		// ---- Expand All Toggle (Toolbar) ----
		let expandBtn = document.createElement("button");
		expandBtn.id = "expandall-btn";
		expandBtn.innerText = "Expand labels";
		expandBtn.style.cssText = `
		  font-size:12px;
		  padding:3px 8px;
		  border-radius:6px;
		  border:1px solid rgba(15,23,42,0.12);
		  background:#f8fafc;
		  color:#0f172a;
		  cursor:pointer;
		`;
		
		expandBtn.onclick = function () {
		  toggleExpandAll(expandBtn);
		};
		
		rightControls.appendChild(orderLabel);
		rightControls.appendChild(sortSelect);
		rightControls.appendChild(expandBtn);
		toolbar.appendChild(leftInfo);
		toolbar.appendChild(rightControls);
		
		body.appendChild(toolbar);
	
	    // ---- Container (cards) ----
	    let container = document.createElement("div");
	    container.id = "variantcontainer";
	    container.style.cssText = `
		    display: flex;
		    flex-direction: column;
		    gap: 12px;
		
		    /* ✅ Single horizontal scrollbar for all variants */
		    overflow-x: auto;
		    overflow-y: visible;
		
		    padding-bottom: 8px;
		`;
		container.style.maxWidth = "100%";
		container.style.boxSizing = "border-box";
	    body.appendChild(container);
	
	    // ---- Buttons Container ----
	    let btnDiv = document.createElement("div");
	    btnDiv.style.cssText =
	        "margin-top:16px;display:flex;gap:10px;align-items:center;";
	
	    // ---- Load More Button ----
	    let loadBtn = document.createElement("button");
	    loadBtn.id = "load-btn";
	    loadBtn.innerText = "Load more (+5)";
	    loadBtn.style.cssText = buttonStyle();
	    loadBtn.onclick = renderNextBatch;
	    btnDiv.appendChild(loadBtn);
	
	    // ---- Export Button ----
	    let exportBtn = document.createElement("button");
	    exportBtn.id = "export-btn";
	    exportBtn.innerText = "Export visible as SVG";
	    exportBtn.style.cssText = buttonStyle("primary");
	    exportBtn.onclick = exportVisibleAsSVG;
	    btnDiv.appendChild(exportBtn);
	
	    body.appendChild(btnDiv);
	
	    updateStatus();
	}


    function buttonStyle(kind) {
        const base =
            "border:1px solid rgba(15,23,42,0.15);border-radius:10px;padding:9px 12px;" +
            "font-size:13px;cursor:pointer;box-shadow:0 1px 2px rgba(0,0,0,0.05);";
        if (kind === "primary") {
            return base + "background:#0f172a;color:white;border-color:#0f172a;";
        }
        return base + "background:white;color:#0f172a;";
    }

    // =====================================================
    // RENDER NEXT 5 VARIANTS (as cards)
    // =====================================================
    function renderNextBatch() {

        let container = document.getElementById("variantcontainer");
        if (!container) return;

        let end = Math.min(_renderIndex + BATCH_SIZE, _allVariants.length);

        for (let i = _renderIndex; i < end; i++) {

            let variant = _allVariants[i];
            let trace = variant.activities || [];
            let freq = variant.frequency || 0;

            let pct = (_totalFrequency > 0)
                ? (freq / _totalFrequency * 100).toFixed(1)
                : "0.0";

            // ---- Card ----
            let card = document.createElement("div");
            card.className = "variant-card";
            card.dataset.variantIndex = String(i);
            card.style.cssText =
			    "background:white;border-radius:14px;" +
			    "box-shadow:0 1px 6px rgba(0,0,0,0.08);" +
			    "border:1px solid rgba(15,23,42,0.08);" +
			    "padding:10px 12px;" +
			
			    "display:inline-flex;" +
			    "width:max-content;" +
			    "align-self:flex-start;" +
			
			    "gap:14px;align-items:center;" +
			    "cursor:pointer;";


            // ---- Order Column ----
            let order = document.createElement("div");
            order.style.cssText =
                "width:56px;flex:0 0 56px;display:flex;flex-direction:column;" +
                "align-items:flex-start;gap:2px;";
            order.innerHTML =
                `<div style="font-weight:700;font-size:14px;">#${i + 1}</div>` +
                `<div style="font-size:11px;color:#64748b;">Variant</div>`;
            card.appendChild(order);

            // ---- Stats Column ----
            let stats = document.createElement("div");
            stats.style.cssText =
                "width:120px;flex:0 0 120px;display:flex;flex-direction:column;gap:2px;";
            stats.innerHTML =
                `<div style="font-weight:700;font-size:13px;">${freq} Cases</div>` +
                `<div style="font-size:12px;color:#64748b;">${pct}%</div>`;
            card.appendChild(stats);

            // ---- Trace Column ----
            let traceWrap = document.createElement("div");
			traceWrap.style.cssText = `
			    flex: 0 0 auto;
			    white-space: nowrap;
			`;
			traceWrap.className = "trace-scroll";
			traceWrap.style.msOverflowStyle = "none"; // IE/Edge
			traceWrap.className = "trace-scroll";
            // initial expanded state for this row
             if (!_expandedRows.has(i)) {
			    _expandedRows.set(i, false);
			  }
			
			  const isExpanded = _expandedRows.get(i);

            let svg = buildVariantSVG(trace, _activityColorMap, isExpanded);

			// Cache SVG string for export (ONE TIME)
			_variantSVGCache.set(i, {
			    index: i + 1,
			    freqText: `${freq} Cases`,
			    pctText: `${pct}%`,
			    svgString: new XMLSerializer().serializeToString(svg)
			});
			
			traceWrap.appendChild(svg);
			card.appendChild(traceWrap);

            // Clicking anywhere toggles expand/collapse for the whole row
            card.onclick = function () {
                toggleExpandCard(card, trace, i);
            };

            container.appendChild(card);
            
            updateExpandAllButton();
        }

        _renderIndex = end;
        updateStatus();

        if (_renderIndex >= _allVariants.length) {
            let btn = document.getElementById("load-btn");
            if (btn) btn.style.display = "none";
        }
    }
    
    function updateExpandAllButton() {

	  let btn = document.getElementById("expandall-btn");
	  if (!btn) return;
	
	  // ✅ Nothing rendered yet → always collapsed state
	  if (_renderIndex === 0) {
	    btn.innerText = "Expand labels";
	    return;
	  }
	
	  let allExpanded = true;
	
	  for (let i = 0; i < _renderIndex; i++) {
	
	    // Treat undefined as collapsed
	    if (_expandedRows.get(i) !== true) {
	      allExpanded = false;
	      break;
	    }
	  }
	
	  btn.innerText = allExpanded ? "Collapse labels" : "Expand labels";
	}
    

    function toggleExpandCard(card, trace, variantIndex) {
        const now = !_expandedRows.get(variantIndex);
        _expandedRows.set(variantIndex, now);

        // Replace SVG inside the card with new expanded/collapsed SVG
        const traceWrap = card.children[2];
        if (!traceWrap) return;

        // remove old
        while (traceWrap.firstChild) traceWrap.removeChild(traceWrap.firstChild);

        // add new
        let newSvg = buildVariantSVG(trace, _activityColorMap, now);

		// Update cache so export reflects expanded/collapsed state
		_variantSVGCache.set(variantIndex, {
		    index: variantIndex + 1,
		    freqText: _variantSVGCache.get(variantIndex).freqText,
		    pctText: _variantSVGCache.get(variantIndex).pctText,
		    svgString: new XMLSerializer().serializeToString(newSvg)
		});
		
		traceWrap.appendChild(newSvg);
    }

    // =====================================================
    // BUILD SVG FOR ONE TRACE (seamless arrows, global hover, expand widths)
    // expanded is controlled by row click (not inside SVG)
    // =====================================================
    function buildVariantSVG(trace, activityColorMap, expanded) {

        function shorten(text, maxLen = 12) {
            return text.length > maxLen ? text.substring(0, maxLen) + "…" : text;
        }

        function computeBlockWidth(label) {
            // fits full label in expanded mode
            return Math.max(140, label.length * 8.7);
        }

        const arrowTip = 20;

        // labels + widths
        const labels = trace.map(a => expanded ? a : shorten(a));
        const widths = labels.map(l => expanded ? computeBlockWidth(l) : 140);

        const totalWidth = widths.reduce((a, b) => a + b, 0) + 40;

        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("height", 55);
        svg.setAttribute("width", totalWidth);
        svg.setAttribute("viewBox", `0 0 ${totalWidth} 55`);
        svg.setAttribute("preserveAspectRatio", "xMinYMin meet");

        // One tooltip layer on top
        const hoverRect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
        hoverRect.setAttribute("fill", "rgba(15,23,42,0.80)");
        hoverRect.setAttribute("rx", "7");
        hoverRect.style.display = "none";

        const hoverText = document.createElementNS("http://www.w3.org/2000/svg", "text");
        hoverText.setAttribute("fill", "white");
        hoverText.setAttribute("font-size", "12px");
        hoverText.setAttribute("font-family", "Arial, sans-serif");
        hoverText.setAttribute("text-anchor", "middle");
        hoverText.style.display = "none";

        // Insert tooltip last so it sits on top
        svg.appendChild(hoverRect);
        svg.appendChild(hoverText);

        let xOffset = 0;

        trace.forEach((activityRaw, i) => {

            const activity = (activityRaw || "").trim();
            const blockWidth = widths[i];
            const labelText = labels[i];

            const fillColor = activityColorMap[activity] || "#E2E8F0"; // soft fallback
            const leftX = (i === 0) ? xOffset + 8 : (xOffset - arrowTip);

            const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");

            if (i === 0) {
                polygon.setAttribute(
                    "points",
                    `${leftX},8
					 ${leftX},45
					 ${xOffset + blockWidth - arrowTip},45
					 ${xOffset + blockWidth},26.5
					 ${xOffset + blockWidth - arrowTip},8`
                );
            } else {
                polygon.setAttribute(
                    "points",
                    `${leftX},8
					 ${xOffset + blockWidth - arrowTip},8
					 ${xOffset + blockWidth},26.5
					 ${xOffset + blockWidth - arrowTip},45
					 ${leftX},45
					 ${xOffset},26.5`
                );
            }

            polygon.style.fill = fillColor;
            // subtle border (less clipping issues than thick white)
            polygon.style.stroke = "rgba(255,255,255,0.85)";
            polygon.style.strokeWidth = "1";
            polygon.style.strokeLinejoin = "round";

            // Put polygon under tooltip
            svg.insertBefore(polygon, hoverRect);

            // Label
            const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
            const centerX = xOffset + blockWidth / 2;

            text.textContent = labelText;
            text.setAttribute("x", centerX);
            text.setAttribute("y", 32);
            text.setAttribute("text-anchor", "middle");
            text.setAttribute("font-size", "13px");
            text.setAttribute("font-family", "Arial, sans-serif");
            text.setAttribute("fill", "#0f172a"); // dark text on soft colors
            svg.insertBefore(text, hoverRect);

            // Hover updates global tooltip (capture centerX/blockWidth, not xOffset)
            polygon.addEventListener("mouseover", function () {

                hoverText.textContent = activity;
                hoverText.setAttribute("x", centerX);
                hoverText.setAttribute("y", 14);

                hoverText.style.display = "block";

                const box = hoverText.getBBox();

                hoverRect.setAttribute("x", box.x - 8);
                hoverRect.setAttribute("y", box.y - 4);
                hoverRect.setAttribute("width", box.width + 16);
                hoverRect.setAttribute("height", box.height + 8);
                hoverRect.style.display = "block";
            });

            polygon.addEventListener("mouseout", function () {
                hoverText.style.display = "none";
                hoverRect.style.display = "none";
            });

            xOffset += blockWidth;
        });

        return svg;
    }

    // =====================================================
    // EXPORT: build a single SVG that matches the UI cards
    // (no domtoimage, no clipping)
    // =====================================================
    function exportVisibleAsSVG() {
        const svgString = buildExportSVGString();
        downloadSVG(svgString, "variants.svg");
    }

    function buildExportSVGString() {

	    const padding = 20;
	    const rowGap = 20;
	    const cardHeight = 72;
	    const cardRadius = 14;
	
	    const orderColW = 70;
	    const statsColW = 140;
	
	    const visible = [];
		for (let i = 0; i < _renderIndex; i++) {
		    const v = _variantSVGCache.get(i);
		    if (v) visible.push(v);
		}
					
	    if (visible.length === 0) return null;
	
	    // Determine max trace width
	    let maxTraceWidth = 0;
	    visible.forEach(v => {
	        const doc = new DOMParser().parseFromString(v.svgString, "image/svg+xml");
	        const svg = doc.documentElement;
	        maxTraceWidth = Math.max(
	            maxTraceWidth,
	            parseFloat(svg.getAttribute("width")) || 0
	        );
	    });
	
	    const width =
	        padding * 2 + orderColW + statsColW + maxTraceWidth + 40;
	
	    const height =
	        padding * 2 + visible.length * cardHeight +
	        (visible.length - 1) * rowGap;
	
	    const root = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	    root.setAttribute("xmlns", "http://www.w3.org/2000/svg");
	    root.setAttribute("width", width);
	    root.setAttribute("height", height);
	
	    // Background
	    const bg = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	    bg.setAttribute("x", 0);
	    bg.setAttribute("y", 0);
	    bg.setAttribute("width", width);
	    bg.setAttribute("height", height);
	    bg.setAttribute("fill", "#f8fafc");
	    root.appendChild(bg);
	
	    let y = padding;
	
	    visible.forEach(v => {
	
	        // Card
	        const card = document.createElementNS("http://www.w3.org/2000/svg", "rect");
	        card.setAttribute("x", padding);
	        card.setAttribute("y", y);
	        card.setAttribute("width", width - 2 * padding);
	        card.setAttribute("height", cardHeight);
	        card.setAttribute("rx", cardRadius);
	        card.setAttribute("fill", "white");
	        card.setAttribute("stroke", "rgba(15,23,42,0.12)");
	        root.appendChild(card);
	
	        // Variant index
	        root.appendChild(svgText(
	            `#${v.index}`,
	            padding + 20,
	            y + 30,
	            14,
	            "#0f172a",
	            700
	        ));
	        root.appendChild(svgText(
	            "Variant",
	            padding + 20,
	            y + 50,
	            11,
	            "#64748b",
	            400
	        ));
	
	        // Stats
	        const sx = padding + orderColW;
	        root.appendChild(svgText(v.freqText, sx + 10, y + 30, 13, "#0f172a", 700));
	        root.appendChild(svgText(v.pctText, sx + 10, y + 50, 12, "#64748b", 500));
	
	        // Trace SVG
	        const parsedSvg = new DOMParser()
			    .parseFromString(v.svgString, "image/svg+xml")
			    .documentElement;
			
			// (event listeners added via addEventListener are NOT serialized anyway,
			// so this is mostly harmless, but keep it if you want)
			stripInteractivity(parsedSvg);
			
			// Move children out of the nested <svg> to avoid nested-svg sizing differences
			const traceGroup = document.createElementNS("http://www.w3.org/2000/svg", "g");
			while (parsedSvg.firstChild) traceGroup.appendChild(parsedSvg.firstChild);
			
			const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
			g.setAttribute("transform", `translate(${padding + orderColW + statsColW},${y + 8})`);
			g.appendChild(traceGroup);
			root.appendChild(g);

	
	        y += cardHeight + rowGap;
	    });
	
	    return new XMLSerializer().serializeToString(root);
	}


    function svgText(text, x, y, fontSize, fill, weight) {
        const t = document.createElementNS("http://www.w3.org/2000/svg", "text");
        t.textContent = text;
        t.setAttribute("x", x);
        t.setAttribute("y", y);
        t.setAttribute("font-size", fontSize);
        t.setAttribute("fill", fill);
        t.setAttribute("font-family", "Arial, sans-serif");
        t.setAttribute("font-weight", weight);
        return t;
    }

    function stripInteractivity(svgEl) {
        const all = svgEl.querySelectorAll("*");
        all.forEach(n => {
            n.onmouseover = null;
            n.onmouseout = null;
            n.onclick = null;
        });
    }

    function estimateTraceWidth(activities) {
        const blockW = 170;
        return activities.length * blockW + 60;
    }

    function downloadSVG(svgString, filename) {
        const blob = new Blob([svgString], { type: "image/svg+xml;charset=utf-8" });
        const url = URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        URL.revokeObjectURL(url);
    }
    
    function toggleExpandAll(button) {

	    // how many rows are currently visible?
	    const visibleCount = _renderIndex;
	
	    // Determine target state: if any visible row is collapsed -> expand all
	    let shouldExpand = false;
	    for (let i = 0; i < visibleCount; i++) {
	        if (!_expandedRows.get(i)) { shouldExpand = true; break; }
	    }
	
	    // Apply to all visible
	    for (let i = 0; i < visibleCount; i++) {
	        _expandedRows.set(i, shouldExpand);
	    }
	
	    // Update button label
	    button.innerText = shouldExpand ? "Collapse labels" : "Expand labels";
	
	    // Re-render EXACTLY the same amount that was visible
	    const container = document.getElementById("variantcontainer");
	    if (!container) return;
	
	    container.innerHTML = "";
	    _renderIndex = 0;
	
	    while (_renderIndex < visibleCount) {
	        renderNextBatch();
	    }
	
	    updateExpandAllButton();
	}
	
	function handleSortClick(field) {

	  if (_sortField === field) {
	    // toggle direction
	    _sortDirection = _sortDirection === "asc" ? "desc" : "asc";
	  } else {
	    // switch field
	    _sortField = field;
	    _sortDirection = "desc";
	  }
	
	  applySorting();
	  updateSortButtons();
	}
	
	function applySorting(mode) {

	  if (mode === "frequency-desc") {
	    _allVariants.sort((a, b) => (b.frequency || 0) - (a.frequency || 0));
	  }
	
	  if (mode === "frequency-asc") {
	    _allVariants.sort((a, b) => (a.frequency || 0) - (b.frequency || 0));
	  }
	
	  if (mode === "length-desc") {
	    _allVariants.sort((a, b) =>
	      (b.activities?.length || 0) - (a.activities?.length || 0)
	    );
	  }
	
	  if (mode === "length-asc") {
	    _allVariants.sort((a, b) =>
	      (a.activities?.length || 0) - (b.activities?.length || 0)
	    );
	  }
	
	  // Reset rendering
	  _expandedRows.clear();
	  _variantSVGCache.clear();
	
	  let container = document.getElementById("variantcontainer");
	  if (container) container.innerHTML = "";
	
	  _renderIndex = 0;
	
	  let loadBtn = document.getElementById("load-btn");
	  if (loadBtn) loadBtn.style.display = "inline-block";
	
	  renderNextBatch();
	}
	
	function updateSortButtons() {

	  document.querySelectorAll("[data-field]").forEach(btn => {
	
	    let field = btn.dataset.field;
	
	    btn.style.background = "white";
	    btn.style.color = "#0f172a";
	
	    if (field === _sortField) {
	      btn.style.background = "#0f172a";
	      btn.style.color = "white";
	
	      btn.innerText =
	        (field === "frequency" ? "Frequency" : "Trace length") +
	        (_sortDirection === "desc" ? " ↓" : " ↑");
	    } else {
	      btn.innerText =
	        field === "frequency" ? "Frequency" : "Trace length";
	    }
	  });
	}

	    

    // =====================================================
    // STATUS UPDATE
    // =====================================================
    function updateStatus() {	 
	  let info = document.getElementById("toolbar-info");
	  if (info) {
	    info.innerText = `Showing ${_renderIndex} of ${_allVariants.length} variants`;
	  }
	}

    // =====================================================
    // HELPERS: Colors
    // =====================================================
    function createActivityColorMap(allActivities) {

        // Soft, modern, extended palette (24)
        const palette = [
            "#A8DADC", "#BFD7EA", "#CDEAC0", "#FFE5B4",
            "#F7CAD0", "#D6CDEA", "#FFD6A5", "#D0F4DE",
            "#E4C1F9", "#E2E8F0",

            "#90CAF9", "#A5D6A7", "#FFF59D", "#FFCCBC",
            "#CE93D8", "#80DEEA", "#FFAB91", "#BCAAA4",

            "#81C784", "#64B5F6", "#BA68C8", "#4DB6AC",
            "#FFD54F", "#E57373"
        ];

        let map = {};
        allActivities.forEach((act, i) => {
            const key = (act || "").trim();
            map[key] = palette[i % palette.length];
        });
        return map;
    }

    return view;

})();
