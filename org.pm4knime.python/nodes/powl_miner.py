import io
import knime.extension as knext
import pandas as pd
import os
import logging
import pytz
import powl
from powl.visualization.powl import visualizer as powl_visualizer
from utils import knime_util
from utils.petri_net_type import PetriNetPortObject, PetriNetSpec, Node, Link 
from utils.petri_net_type import petri_net_to_df
from pm4py.objects.petri_net.exporter.variants.pnml import export_petri_tree, Parameters
from pm4py.util import exec_utils, constants
from utils.petri_net_type import convert_pm4py_to_port_object


LOGGER = logging.getLogger(__name__)

script_dir = os.path.dirname(os.path.abspath(__file__))
path_to_icon = os.path.abspath(os.path.join(script_dir, "..", "icon", "category-discovery.png"))

petri_net_port_type = knext.nodes.get_port_type_for_id(
    "org.pm4knime.portobject.PetriNetPortObject"
)


@knext.node(name="POWL Miner",
            node_type=knext.NodeType.LEARNER,
            icon_path=path_to_icon,
            category="/community/processmining/discovery")
@knime_util.create_node_description(
    short_description="Discover a Partially Ordered Workflow Model (POWL 2.0) from an event table.",
    description="Discover a Partially Ordered Workflow Model (POWL 2.0) from an event log.")
@knext.input_table(name="Event Table", description="An Event Table.")
@knext.output_port(name="Petri Net", description="A Petri Net.", port_type=petri_net_port_type)
@knext.output_image(name="POWL Model", description="An SVG image of a POWL model.")
@knext.output_view(name="POWL Model", description="A POWL model.")

class POWL_Miner(knext.PythonNode):
    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_activity = knext.ColumnParameter(label="Activity Column",
                                                  description="The column that contains the activities.",
                                                  port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps.",
                                              port_index=0,
                                              column_filter=knime_util.is_type_timestamp)
    column_param_threshold = knext.DoubleParameter(label="Noise Filtering Threshold (0.0 = No Filtering)",
                                                   description="Set the threshold for DFG frequency filtering.",
                                                   default_value=0.0,
                                                   min_value=0.0,
                                                   max_value=1.0)

    def configure(self, configure_context: knext.ConfigurationContext, input_schema_1: knext.Schema):
        for par in [self.column_param_case, self.column_param_time, self.column_param_activity]:
            if par is None or par == "":
                raise knext.InvalidParametersError("Parameters not set! Please configure the node!")
        return None


    def execute(self, exec_context, input_1):
        event_log = input_1.to_pandas()
    
        event_log.drop(
            columns=[c for c in event_log.columns 
                     if c not in [self.column_param_case, self.column_param_activity, self.column_param_time]],
            inplace=True
        )
        
        event_log.rename(
            columns={self.column_param_case: 'case:concept:name', 
                     self.column_param_activity: 'concept:name', 
                     self.column_param_time: 'time:timestamp'},
            inplace=True)
    
        event_log["time:timestamp"] = pd.to_datetime(event_log["time:timestamp"], utc=True)
    
        event_log = event_log.sort_values(by=["case:concept:name", "time:timestamp"])
    
        powl_model = powl.discover(event_log, dfg_frequency_filtering_threshold=self.column_param_threshold)
        
        pn_1, init_1, final_1 = powl.convert_to_petri_net(powl_model)
    
        petri_net_port_object = convert_pm4py_to_port_object(pn_1, init_1, final_1)
    
        powl_vis = powl_visualizer.apply(powl_model)
    
        return petri_net_port_object, powl_vis, knext.view_svg(powl_vis)