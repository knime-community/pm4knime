package org.pm4knime.node.visualizations.logviews.tracevariant;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.BundlePageResources;

public class TraceVariantVisNodeFactory extends DefaultNodeFactory {

    private static final String TRACE_VARIANT_PAGE = "src/views/tracevariant/index.html";

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
            	    .page(p -> BundlePageResources.createPage(TRACE_VARIANT_PAGE))
            	    .initialData(TraceVariantView::createInitialData)
            	)

                .nodeType(NodeType.Visualizer)
        );
    }
    
}


