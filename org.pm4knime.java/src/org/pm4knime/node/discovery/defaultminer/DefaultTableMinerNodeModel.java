package org.pm4knime.node.discovery.defaultminer;

import java.util.Arrays;
import java.util.OptionalInt;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.data.sort.RowComparator;
import org.knime.core.data.sort.RowComparator.ColumnComparatorBuilder;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;
import org.pm4knime.node.visualizations.jsgraphviz.util.WebUIJSViewNodeModel;
import org.pm4knime.portobject.AbstractJSONPortObject;


public abstract class DefaultTableMinerNodeModel<S extends DefaultTableMinerSettings> extends WebUIJSViewNodeModel<S, JSGraphVizViewRepresentation, JSGraphVizViewValue> implements PortObjectHolder {

	protected DefaultTableMinerNodeModel(PortType[] inPortTypes, PortType[] outPortTypes, String view_name, Class<S> modelSettingsClass) {
		super(inPortTypes, outPortTypes, view_name, modelSettingsClass);
	}

	
	protected BufferedDataTable logPO;
	protected AbstractJSONPortObject pmPO;
	protected S m_settings;
	
	
	@Override
    protected PortObject[] performExecuteCreatePortObjects(final PortObject svgImageFromView,
        final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        return new PortObject[]{pmPO};
    }
	
	@Override
	protected void performExecuteCreateView(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		logPO = (BufferedDataTable)inObjects[0];
        final var dts = logPO.getDataTableSpec();
        String[] sorting_columns = {m_settings.t_classifier, m_settings.time_classifier};
        final var sorter = new BufferedDataTableSorter(logPO, toRowComparator(dts, sorting_columns));
        sorter.setSortInMemory(false);
        final BufferedDataTable sortedTable = sorter.sort(exec);
        logPO = sortedTable;               
		pmPO = mine(logPO, exec);
		
		JSGraphVizViewRepresentation representation = getViewRepresentation();
		representation.setJSONString(pmPO.getJSON());

	}
	
	public static RowComparator toRowComparator(final DataTableSpec spec, String[] sorting_columns) {
        final var rc = RowComparator.on(spec);
        Arrays.stream(sorting_columns).forEach(column -> {
            final var ascending = true;
            final var alphaNum = true;
            resolveColumnName(spec, column).ifPresentOrElse(
                col -> rc.thenComparingColumn(col,
                    c -> configureColumnComparatorBuilder(spec, ascending, alphaNum, col, c)),
                () -> rc.thenComparingRowKey(
                    k -> k.withDescendingSortOrder(!ascending).withAlphanumericComparison(alphaNum)));
        });
        return rc.build();
    }
	
	private static ColumnComparatorBuilder configureColumnComparatorBuilder(final DataTableSpec spec, final boolean ascending, final boolean alphaNum, final int col,
	        final ColumnComparatorBuilder c) {
	        var compBuilder = c.withDescendingSortOrder(!ascending);
	        if (spec.getColumnSpec(col).getType().isCompatible(StringValue.class)) {
	            compBuilder.withAlphanumericComparison(alphaNum);
	        }
	        return compBuilder.withMissingsLast(false);
	    }
	
	private static OptionalInt resolveColumnName(final DataTableSpec dts, final String colName) {
	        final var idx = dts.findColumnIndex(colName);
	        if (idx == -1) {
	            return OptionalInt.empty();
	        }
	        return OptionalInt.of(idx);
	    }


	protected abstract AbstractJSONPortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception; 
	
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs, final S modelSettings) throws InvalidSettingsException {

		m_settings = modelSettings;
		
		if (inSpecs[0] == null) {
            return new PortObjectSpec[]{null};
        }

        if (!(inSpecs[0] instanceof DataTableSpec)) {
            throw new InvalidSettingsException("Input port must be connected to a data table.");
        }

		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		if(modelSettings.e_classifier == null || modelSettings.t_classifier == null || modelSettings.time_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set! Please open the dialog and configure the node!");
		return configureOutSpec(logSpec);
	}


	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec);	
		
	
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {logPO};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		logPO = (BufferedDataTable) portObjects[0];
	}

	
	@Override
	protected void performReset() {
	}

	@Override
	protected void useCurrentValueAsDefault() {
	}

	
	@Override
    protected boolean generateImage() {
        return false;
    }
	
	
	@Override
	public JSGraphVizViewRepresentation createEmptyViewRepresentation() {
		return new JSGraphVizViewRepresentation();
	}

	@Override
	public JSGraphVizViewValue createEmptyViewValue() {
		return new JSGraphVizViewValue();
	}
	
	@Override
	public boolean isHideInWizard() {
		return false;
	}

	@Override
	public void setHideInWizard(boolean hide) {
	}

	@Override
	public ValidationError validateViewValue(JSGraphVizViewValue viewContent) {
		return null;
	}

	@Override
	public void saveCurrentValue(NodeSettingsWO content) {
	}
	
	
	@Override
	public String getJavascriptObjectID() {
		return "org.pm4knime.node.visualizations.jsgraphviz.component";
	}
	
}
