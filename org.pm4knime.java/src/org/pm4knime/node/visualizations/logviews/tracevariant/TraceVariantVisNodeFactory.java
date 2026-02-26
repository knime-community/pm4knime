package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;

public class TraceVariantVisNodeFactory extends DefaultNodeFactory {

    public TraceVariantVisNodeFactory() {
        super(

            DefaultNode.create()

                // --------------------------------------------------
                // REQUIRED PROPERTIES
                // --------------------------------------------------
                .name("Trace Variant Explorer")
                .icon("trace.png")
                .shortDescription("Trace Variant Explorer")
                .fullDescription("This node implements the trace variant explorer. "
                        + "The trace variant explorer represents an event log as a multi-set "
                        + "of unique activity sequences (called trace variants).")

                .sinceVersion(2, 0, 0)

                .ports(p -> p
                    .addInputTable("Event Table","an event table")//
        			.addOutputTable("Variant Summary Table", "a trace variant table")//
        			.addOutputTable("Event Table With Variant IDs", "the input event table extended with an additional variant ID column")//
                )

                .model(m -> m
                    .parametersClass(TraceVariantVisNodeSettings.class)
                    .configure(TraceVariantModel::configure)
                    .execute(TraceVariantModel::execute)
                )

                // --------------------------------------------------
                // OPTIONAL
                // --------------------------------------------------
                .addView(v -> v
                	.withoutParameters()
            	    .description("Trace Variant Explorer")
            	    .page(p -> p
            	        .fromFile()
        	            .bundleClass(TraceVariantVisNodeFactory.class)
        	            .basePath("js-src/dist")
        	            .relativeFilePath("src/views/tracevariant/index.html")
                        .addResourceDirectory("assets")
            	    )
            	    .initialData(TraceVariantView::createInitialData)
            	)

                .nodeType(NodeType.Visualizer)
        );
    }
    
}


