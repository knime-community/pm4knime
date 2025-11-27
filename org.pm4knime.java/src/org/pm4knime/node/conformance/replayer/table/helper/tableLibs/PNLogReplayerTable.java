package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.text.NumberFormat;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import nl.tue.astar.AStarException;

public class PNLogReplayerTable {
//	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
//	public PNRepResult replayLog(PluginContext context, Petrinet net, TableEventLog log, TransEvClassMappingTable mapping,
//			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
//		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
//	}
//	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
//	public PNRepResult replayLog(PluginContext context, ResetNet net, TableEventLog log, TransEvClassMappingTable mapping,
//			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
//		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
//	}
//	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
//	public PNRepResult replayLog(PluginContext context, ResetInhibitorNet net, TableEventLog log, TransEvClassMappingTable mapping,
//			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
//		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
//	}
//	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
//	public PNRepResult replayLog(PluginContext context, InhibitorNet  net, TableEventLog log, TransEvClassMappingTable mapping,
//			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
//		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
//	}
	
	public PNRepResult replayLog(PluginContext context, ExecutionContext exec, PetrinetGraph  net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, exec, net, log, mapping, selectedAlg, parameters);
	}

	/**
	 * Main method to replay log.
	 * 
	 * @param context
	 * @param exec 
	 * @param net
	 * @param log
	 * @param mapping
	 * @param selectedAlg
	 * @param parameters
	 * @return
	 * @throws AStarException
	 * @throws CanceledExecutionException 
	 */
	private PNRepResult replayLogPrivate(PluginContext context, ExecutionContext exec, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		if (selectedAlg.isAllReqSatisfied(context, net, log, mapping, parameters)) {
			// for each trace, replay according to the algorithm. Only returns two objects
			PNRepResult replayRes = null;

			if (parameters.isGUIMode()) {
				long start = System.nanoTime();

				replayRes = selectedAlg.replayLog(context, exec, net, log, mapping, parameters);

				long period = System.nanoTime() - start;
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

				context.log("Replay is finished in " + nf.format(period / 1000000000) + " seconds");
			} else {
				replayRes = selectedAlg.replayLog(context, exec, net, log, mapping, parameters);
			}

			// add connection
			if (replayRes != null) {
				if (parameters.isCreatingConn()) {
					createConnections(context, net, log, mapping, selectedAlg, parameters, replayRes);
				}
			}

			return replayRes;
		} else {
			if (context != null) {
				context.log("The provided parameters is not valid for the selected algorithm.");
				context.getFutureResult(0).cancel(true);
			}
			return null;
		}
	}

	protected void createConnections(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters, PNRepResult replayRes) {
		context.addConnection(new PNRepResultAllRequiredParamConnectionTable(
				"Connection between replay result, Log" + ", and " + net.getLabel(), net, log, mapping, selectedAlg, parameters, replayRes));
	}

}
