let tb_flag = 0;
let process_tree_flag = 0;

(jsgraphviz = function() {

	let _representation;
	let _value;
	let _paper;

	let view = {};

	view.init = function(representation, value) {
		_representation = representation;
		_value = value;

		if (!document.getElementById('loading')) {
			const loadingDiv = document.createElement('div');
			loadingDiv.id = 'loading';

			const spinnerDiv = document.createElement('div');
			spinnerDiv.className = 'spinner';

			loadingDiv.appendChild(spinnerDiv);
			document.body.appendChild(loadingDiv);
		}

		document.getElementById('loading').style.display = 'block';

		setTimeout(() => {
			document.getElementById('loading').style.display = 'none';

			let jsonDataFromJava = JSON.parse(representation.json);
			let nodes = jsonDataFromJava.nodes;
			let edges = jsonDataFromJava.links;
			let xml = jsonDataFromJava.xml;
			let layouter = jsonDataFromJava.layouter;

			if (xml) {
				let xmlString = xml[0];
				createBpmn(xmlString, layouter[0]);
			} else {
				createGraphElements();
				_paper = createPaper(nodes, edges);
			}
		}, 200);
	};

	view.getComponentValue = () => {
		return _value;
	};

	view.validate = function() {
		return true;
	};

	view.getSVG = () => {
		if (_paper) {
			return createSVG(_paper);
		} else {
			return null;
		}
	};

	return view;
}());
