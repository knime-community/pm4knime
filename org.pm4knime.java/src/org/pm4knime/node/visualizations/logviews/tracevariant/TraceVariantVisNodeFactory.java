package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;


public class TraceVariantVisNodeFactory extends WebUINodeFactory<TraceVariantVisNodeModel> implements WizardNodeFactoryExtension<TraceVariantVisNodeModel, TraceVariantVisViewRepresentation, TraceVariantVisViewValue> {
	
	TraceVariantVisNodeModel node;

	public static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()
			.name("Trace Variant Explorer")
			.icon("trace.png")
			.shortDescription("Trace Variant Explorer")
			.fullDescription("This node implements the trace variant explorer. The trace variant explorer represents an event log as a multi-set of unique activity sequences (called trace variants).") 
			.modelSettingsClass(TraceVariantVisNodeSettings.class)//
			.addInputPort("Table", BufferedDataTable.TYPE ,"an event table")//
			.addOutputPort("Image", ImagePortObject.TYPE, "an SVG image")//
			.nodeType(NodeType.Visualizer)
			.build();


	public TraceVariantVisNodeFactory() {
		super(CONFIG);
	}


	protected TraceVariantVisNodeFactory(final WebUINodeConfiguration configuration) {
		super(configuration);
	}


	@Override
	public TraceVariantVisNodeModel createNodeModel() {
		node = new TraceVariantVisNodeModel(TraceVariantVisNodeSettings.class);
		return node;
	}
}