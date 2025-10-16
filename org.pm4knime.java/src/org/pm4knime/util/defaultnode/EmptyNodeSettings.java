package org.pm4knime.util.defaultnode;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.Effect.EffectType;

 
public final class EmptyNodeSettings implements NodeParameters {
	
	static final class NodeSignal implements ParameterReference<Boolean> {}
	
	public static interface EmptyLayout {
				
	    @Section(title = "This node has no settings!")
        interface Text {
	    	
        }   
	      
	 }
	
	static final class AlwaysTrue implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getBoolean(NodeSignal.class).isTrue();
        }
    }

	
	@Layout(EmptyLayout.Text.class)
	@Effect(predicate = AlwaysTrue.class, type = EffectType.HIDE)
	@Widget(title = ".", description = ".")
	@ValueReference(NodeSignal.class)
	boolean dummy_var = true;
	}