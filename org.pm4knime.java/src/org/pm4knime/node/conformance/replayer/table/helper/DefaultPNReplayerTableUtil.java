package org.pm4knime.node.conformance.replayer.table.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNManifestReplayerParameterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ReplayerUtilTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TableEventLog;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TransClass2PatternMapTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.CostBasedCompleteParamTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.EvClassPatternTable;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;

public class DefaultPNReplayerTableUtil {

	public static class ParameterGenerator {

		public static PNManifestReplayerParameterTable getPerfParameter(TableEventLog log, AcceptingPetriNet anet,
				int[] move_costs, Map<String, Integer>[] cost_maps, ExecutionContext exec) throws CanceledExecutionException {

			int log_move_cost = move_costs[0];
			int model_move_cost = move_costs[1];
			int sync_move_cost = move_costs[2];

			Map<String, Integer> log_cost_map = cost_maps[0];
			Map<String, Integer> model_cost_map = cost_maps[1];
			Map<String, Integer> sync_cost_map = cost_maps[2];

			PNManifestReplayerParameterTable parameters = new PNManifestReplayerParameterTable();

			Collection<String> eventClasses = Arrays.asList(log.getActivties());
			TransClasses tc = new TransClasses(anet.getNet());
			Map<TransClass, Set<EvClassPatternTable>> pattern = ReplayerUtilTable.buildPattern(tc, eventClasses);
			TransClass2PatternMapTable mapping = new TransClass2PatternMapTable(log, anet.getNet(), tc, pattern);

			Set<TransClass> tauTC = new HashSet();
			for (Transition t : anet.getNet().getTransitions()) {
				exec.checkCanceled();
				if (t.isInvisible())
					tauTC.add(mapping.getTransClassOf(t));
			}

			/* set the log move cost */
			Map<String, Integer> mapLogCost = new HashMap<String, Integer>();
			for (String c : eventClasses) {
				exec.checkCanceled();
				mapLogCost.put(c, log_cost_map.getOrDefault(c, log_move_cost));
			}

			/* set the model move cost */
			Map<TransClass, Integer> mapModelCost = new HashMap<TransClass, Integer>();
			for (TransClass c : tc.getTransClasses()) {
				if (tauTC.contains(c))
					mapModelCost.put(c, 0);
				else {
					mapModelCost.put(c, model_cost_map.getOrDefault(c.getId(), model_move_cost));
				}
			}

			/* set the sync move cost */
			Map<TransClass, Integer> mapSyncCost = new HashMap<TransClass, Integer>();
			for (TransClass c : tc.getTransClasses()) {
				if (tauTC.contains(c))
					mapSyncCost.put(c, 0);
				else
					mapSyncCost.put(c, sync_cost_map.getOrDefault(c.getId(), sync_move_cost));
			}

			parameters.setMapping(mapping);
			parameters.setMapEvClass2Cost(mapLogCost);
			parameters.setTrans2Cost(mapModelCost);
			parameters.setTransSync2Cost(mapSyncCost);

			/* set markings */
			parameters.setInitMarking(anet.getInitialMarking());
			Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
			int i = 0;
			for (Marking m : anet.getFinalMarkings())
				fmList[i++] = m;

			parameters.setFinalMarkings(fmList);

			parameters.setGUIMode(false);

			return parameters;
		}

		public static IPNReplayParameter getConfParameter(TableEventLog log, AcceptingPetriNet anet,
				String evClassDummy, int[] move_costs, Map<String, Integer>[] cost_maps) throws CanceledExecutionException {

			int log_move_cost = move_costs[0];
			int model_move_cost = move_costs[1];
			int sync_move_cost = move_costs[2];

			Map<String, Integer> log_cost_map = cost_maps[0];
			Map<String, Integer> model_cost_map = cost_maps[1];
			Map<String, Integer> sync_cost_map = cost_maps[2];

			Collection<String> eventClasses = Arrays.asList(log.getActivties());

			Set<Transition> tauTC = new HashSet();
			for (Transition t : anet.getNet().getTransitions()) {
//				exec.checkCanceled();
				if (t.isInvisible())
					tauTC.add(t);
			}

			Map<String, Integer> mapLogCost = new HashMap<String, Integer>();
			for (String c : eventClasses) {
//				exec.checkCanceled();
				mapLogCost.put(c, log_cost_map.getOrDefault(c, log_move_cost));
			}
			mapLogCost.put(evClassDummy, log_move_cost);

			Map<Transition, Integer> mapModelCost = new HashMap<Transition, Integer>();
			for (Transition t : anet.getNet().getTransitions()) {
//				exec.checkCanceled();
				if (tauTC.contains(t))
					mapModelCost.put(t, 0);
				else {
					mapModelCost.put(t, model_cost_map.getOrDefault(t.getLabel(), model_move_cost));
				}
			}

			Map<Transition, Integer> mapSyncCost = new HashMap<Transition, Integer>();
			for (Transition t : anet.getNet().getTransitions()) {
//				exec.checkCanceled();
				if (tauTC.contains(t))
					mapSyncCost.put(t, 0);
				else
					mapSyncCost.put(t, sync_cost_map.getOrDefault(t.getLabel(), sync_move_cost));
			}

			IPNReplayParameter parameters = new CostBasedCompleteParamTable(mapLogCost, mapModelCost, mapSyncCost);

			parameters.setInitialMarking(anet.getInitialMarking());

			Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
			int i = 0;
			for (Marking m : anet.getFinalMarkings())
				fmList[i++] = m;

			parameters.setFinalMarkings(fmList);
			parameters.setGUIMode(false);
			parameters.setCreateConn(false);

			return parameters;
		}

	}

}
