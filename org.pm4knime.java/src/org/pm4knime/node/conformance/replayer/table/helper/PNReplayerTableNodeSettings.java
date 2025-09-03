package org.pm4knime.node.conformance.replayer.table.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.webui.node.dialog.defaultdialog.widget.DomainValuesProvider;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.EffectPredicateProvider.PredicateInitializer;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.TimeColumnsProvider;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerSettings.StringCellColumnsProvider;



@SuppressWarnings("restriction")
public final class PNReplayerTableNodeSettings implements NodeParameters {
	

	//  For enabling custom activity costs
	static final class CustomLogMoveCostsRef implements ParameterReference<Boolean> {}
    static final class CustomModelMoveCostsRef implements ParameterReference<Boolean> {}
    static final class CustomSynchronousMoveCostsRef implements ParameterReference<Boolean> {}
    
    
    // Predicate providers to control visibility based on the new References
    static final class IsCustomLogCostsEnabled implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getBoolean(CustomLogMoveCostsRef.class).isTrue();
        }
    }

    static final class IsCustomModelCostsEnabled implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getBoolean(CustomModelMoveCostsRef.class).isTrue();
        }
    }

    static final class IsCustomSyncCostsEnabled implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getBoolean(CustomSynchronousMoveCostsRef.class).isTrue();
        }
    }

    
	
	public static interface DialogLayout{
	    	
		
		@Section(title = "Replayer Settings")
        interface ReplayerSettings {
        }  
		
		@Section(title = "Log Move Costs")
        @After(ReplayerSettings.class)
        interface LogMoves {
        } 
		
		@Section(title = "Model Move Costs")
        @After(LogMoves.class)
        interface ModelMoves {
        } 
		
		@Section(title = "Synchronous Move Costs")
        @After(ModelMoves.class)
        interface SynchronousMoves {
        } 
	    
	}

	
	
	@Layout(DialogLayout.ReplayerSettings.class)
	@Widget(title = "Case ID", description = "The column that contains the case/trace identifiers.")
    @ChoicesProvider(value = StringCellColumnsProvider.class)
	String t_classifier;
	
	static final class SelectedColumnDependency implements ParameterReference<String> {
    }
	
	@Layout(DialogLayout.ReplayerSettings.class)
	@Widget(title = "Activity", description = "The column that contains the activity/event identifiers.")
	@ValueReference(SelectedColumnDependency.class)
	@ChoicesProvider(value = StringCellColumnsProvider.class)
	String e_classifier;
	
	@Layout(DialogLayout.ReplayerSettings.class)
	@Widget(title = "Timestamp", description = "The column that contains the timestamps.")
    @ChoicesProvider(value = TimeColumnsProvider.class)
	String time_classifier;
	
	
	public static final List<String> strategyList = Arrays.asList(ReplayerUtil.strategyList);
		
		
	public static class StrategyListChoicesProvider implements StringChoicesProvider {
		@Override
		public List<String> choices(final NodeParametersInput context) {
		    return strategyList;
		}
	}	
	
		
	@Widget(title = "Replay Strategy", description = "The replay strategy to be applied. The following options are available:\r\n"
			+ "				<ul>\r\n"
			+ "				<li>\r\n"
			+ "					ILP Replayer: this repalyer calculates the optimal alignment based on ILP.\r\n"
			+ "				</li>\r\n"
			+ "				<li> Non-ILP Replayer.\r\n"
			+ "				</li>\r\n"
			+ "				<li>\r\n"
			+ "					A*-ILP Based Manifest Replayer: this replayer is reserved for performance checking. It generates\r\n"
			+ "					the Manifest to wrap the alignments.\r\n"
			+ "				</li>\r\n"
			+ "				</ul>")
	@Layout(DialogLayout.ReplayerSettings.class)
	@ChoicesProvider(value = StrategyListChoicesProvider.class)
	String strategy;	
	
	static final class SelectedColumnDomainValuesProvider implements DomainValuesProvider {

        Supplier<String> m_selectedColumnSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_selectedColumnSupplier = initializer.computeFromValueSupplier(SelectedColumnDependency.class);
        }

        @Override
        public String getSelectedColumn() {
            return m_selectedColumnSupplier.get();
        }
    }


	static final class SelectedColumnDomainChoicesStateProviderOnInitAndDepChange
	    implements StringChoicesProvider {
	
	    private Supplier<List<String>> m_domainValues;
	
	    @Override
	    public List<String> choices(final NodeParametersInput context) {
	        return m_domainValues.get();
	    }
	
	    @Override
	    public void init(final StateProviderInitializer initializer) {
	        m_domainValues = initializer.computeFromProvidedState(SelectedColumnDomainValuesProvider.class);
	        initializer.computeAfterOpenDialog();
	    }
	
	}


	//	Log Move Costs
   
    @Layout(DialogLayout.LogMoves.class)
	@Widget(title = "Log Move Cost", description = "The default cost for a log move in a non-negative\r\n"
			+ "	integer. By default, it is 1.")
    @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
	int log_move_cost = 1;	 
    
    @Layout(DialogLayout.LogMoves.class)
	@Widget(title = "Enable Custom Log Move Costs", description = """
	          Enable to assign custom costs for single log moves, overriding the default log move cost.
	          """)
    @ValueReference(CustomLogMoveCostsRef.class) 
	boolean custom_log_costs = false;
    
    @Layout(DialogLayout.LogMoves.class)
    @Widget(title = "Custom Log Move Costs", description = "Assign custom costs for single log moves, overriding the default log move cost.")
    @ArrayWidget(addButtonText = "Custom Log Move Costs")
    @Effect(predicate = IsCustomLogCostsEnabled.class, type = EffectType.SHOW)
    public LogMoveCosts[] log_move_costs = new LogMoveCosts[0];
    
    static final class LogMoveCosts implements NodeParameters {

        @HorizontalLayout
        interface LogMoveCostsLayout {
        }

        @Widget(title = "Log Move", description = "")
        @ChoicesProvider(value = SelectedColumnDomainChoicesStateProviderOnInitAndDepChange.class)
        @Layout(LogMoveCostsLayout.class)
        public String log_move;

        @Widget(title = "Cost", description = "")
        @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
        @Layout(LogMoveCostsLayout.class)
        public int cost = 1;
    }
    
    
    //	Model Move Costs
    
    public static final class ModelMoveChoices implements StringChoicesProvider {

	    @Override
	    public List<String> choices(final NodeParametersInput context) {
	
	        Object specObj = context.getInPortSpecs()[1];
	        
	        if (specObj == null) {
	            return Collections.emptyList(); 
	        }

	        if (specObj instanceof PetriNetPortObjectSpec) { // Check if the object is an instance of DataTableSpec
	        	PetriNetPortObjectSpec specs = (PetriNetPortObjectSpec) specObj;

	        	return specs.getTransitions().stream()
	                    .filter(s -> !s.isInvisible())
	                    .map(s -> s.getLabel())
	                    .collect(Collectors.toList());
	        } else {
	            System.err.println("Expected a PetriNetPortObjectSpec but received a different type: " + specObj.getClass().getSimpleName());
	            return Collections.emptyList(); 
	        }
	    }
	}
    
    @Layout(DialogLayout.ModelMoves.class)
	@Widget(title = "Model Move Cost", description = "The default cost for a model move in a non-negative\r\n"
			+ "	integer. By default, it is 1.")
    @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
	int model_move_cost = 1;	
    
    
    @Layout(DialogLayout.ModelMoves.class)
	@Widget(title = "Enable Custom Model Move Costs", description = """
	          Enable to assign custom costs for single model moves, overriding the default model move cost.
	          """)
    @ValueReference(CustomModelMoveCostsRef.class)
	boolean custom_model_costs = false;
    
    @Layout(DialogLayout.ModelMoves.class)
    @Widget(title = "Custom Model Move Costs", description = "Assign custom costs for single model moves, overriding the default model move cost.")
    @ArrayWidget(addButtonText = "Custom Model Move Costs")
    @Effect(predicate = IsCustomModelCostsEnabled.class, type = EffectType.SHOW)
    public ModelMoveCosts[] model_move_costs = new ModelMoveCosts[0];
    
    static final class ModelMoveCosts implements NodeParameters {

        @HorizontalLayout
        interface ModelMoveCostsLayout {
        }

        @Widget(title = "Model Move", description = "")
        @ChoicesProvider(value = ModelMoveChoices.class)
        @Layout(ModelMoveCostsLayout.class)
        public String model_move;

        @Widget(title = "Cost", description = "")
        @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
        @Layout(ModelMoveCostsLayout.class)
        public int cost = 1;
    }
    
    
    //	Sync Move Costs
    
    @Layout(DialogLayout.SynchronousMoves.class)
	@Widget(title = "Synchronous Move Cost", description = "The default cost for a Synchronous move in a non-negative\r\n"
			+ "	integer. By default, it is 0.")
    @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
	int sync_move_cost = 0;	 
    
    @Layout(DialogLayout.SynchronousMoves.class)
	@Widget(title = "Enable Custom Synchronous Move Costs", description = """
	          Enable to assign custom costs for single synchronous moves, overriding the default synchronous move cost.
	          """)
	@ValueReference(CustomSynchronousMoveCostsRef.class) 
	boolean custom_sync_costs = false;
    
    @Layout(DialogLayout.SynchronousMoves.class)
    @Widget(title = "Custom Synchronous Move Costs", description = "Assign custom costs for single synchronous moves, overriding the default synchronous move cost.")
    @ArrayWidget(addButtonText = "Custom Synchronous Move Costs")
    @Effect(predicate = IsCustomSyncCostsEnabled.class, type = EffectType.SHOW)
    public SynchronousMoveCosts[] sync_move_costs = new SynchronousMoveCosts[0];
    
    
    static final class SynchronousMoveCosts implements NodeParameters {

        @HorizontalLayout
        interface SynchronousMoveCostsLayout {
        }

        @Widget(title = "Synchronous Move", description = "")
        @ChoicesProvider(value = ModelMoveChoices.class)
        @Layout(SynchronousMoveCostsLayout.class)
        public String sync_move;

        @Widget(title = "Cost", description = "")
        @NumberInputWidget(minValidation=IsNonNegativeValidation.class)
        @Layout(SynchronousMoveCostsLayout.class)
        public int cost = 0;
        
    }
    
   
	public Map<String, Integer>[] getCostMaps() {
    			
		Map<String, Integer> log_cost_map = new HashMap<>();
		if (custom_log_costs) {;
			log_cost_map = Arrays.stream(log_move_costs)
            .collect(Collectors.toMap(
                smc -> smc.log_move, 
                smc -> smc.cost
            ));
		}					
				
		/* set the model move cost */
		Map<String, Integer> model_cost_map = new HashMap<>();
		if (custom_model_costs) {	
			model_cost_map = Arrays.stream(model_move_costs)
            .collect(Collectors.toMap(
                smc -> smc.model_move, 
                smc -> smc.cost
            ));
		}
				
		 
		/* set the sync move cost */
		Map<String, Integer> sync_cost_map = new HashMap<>();
		if (custom_sync_costs) {
			sync_cost_map = Arrays.stream(sync_move_costs)
            .collect(Collectors.toMap(
                smc -> smc.sync_move, 
                smc -> smc.cost
            ));
		}
		
		
		Map<String, Integer>[] move_cost_maps = new HashMap[3];
		move_cost_maps[0] = log_cost_map;
		move_cost_maps[1] = model_cost_map;
		move_cost_maps[2] = sync_cost_map;
		
		return move_cost_maps;
		
    }
	
    
}