package org.pm4knime.node.conversion.hpn2table;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.Widget;


public final class HybridPetriNet2TableConverterNodeSettings implements NodeParameters {
	 
	public static interface HybridPetriNet2TableDialogLayout {
	    		
	    interface RowIdentifier {
	    	
        }
	    
	    @After(RowIdentifier.class)
	    interface ColumnName {

	    }
	}
	
//	public static class RowIdentifierChoicesProvider implements ChoicesProvider {
//        @Override
//        public String[] choices(final DefaultNodeSettingsContext context) {
//        	HybridPetriNet2TableConverterNodeModel node = new HybridPetriNet2TableConverterNodeModel(HybridPetriNet2TableConverterNodeSettings.class);
//        	ArrayList<String> rowList = new ArrayList<>(1);
//            rowList.add(node.DEFAULT_ROWKEY.toString());
//            String rowListArray[] = rowList.toArray(new String[rowList.size()]);
//            return rowListArray;
//        }
//    }

	@Widget(title = "Row Identifier", description = "Identifier for the row generated")
	@Layout(HybridPetriNet2TableDialogLayout.RowIdentifier.class)
//	@ChoicesWidget(choices = RowIdentifierChoicesProvider.class)
	@TextInputWidget
	String m_row_identifier = "row 0";
	
	@Widget(title = "Column Name", description = "Name of the column generated")
	@Layout(HybridPetriNet2TableDialogLayout.ColumnName.class)
	@TextInputWidget
	String m_column_name = "Hybrid Petri Net";
}