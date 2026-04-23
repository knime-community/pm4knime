import io
import knime.extension as knext
import pandas as pd
import os
import html
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


def _exception_to_svg(exc: Exception, width: int = 1000, height: int = 260) -> str:
    """
    Create a simple SVG that displays the visualization error.
    This keeps image/view outputs valid even if Graphviz is unavailable.
    """
    title = "POWL visualization could not be generated"
    msg = f"{type(exc).__name__}: {str(exc)}"

    title = html.escape(title)
    msg = html.escape(msg)

    # crude line wrapping for long exception messages
    max_chars = 95
    lines = [msg[i:i + max_chars] for i in range(0, len(msg), max_chars)]
    tspan_lines = "\n".join(
        f'<tspan x="30" dy="22">{line}</tspan>' for line in lines
    )

    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}">
  <rect width="100%" height="100%" fill="#fff8f8" stroke="#cc0000" stroke-width="2"/>
  <text x="30" y="45" font-family="Arial, Helvetica, sans-serif" font-size="22" font-weight="bold" fill="#aa0000">
    {title}
  </text>
  <text x="30" y="85" font-family="Arial, Helvetica, sans-serif" font-size="16" fill="#222">
    <tspan x="30" dy="0">The POWL model was generated successfully, but rendering it failed.</tspan>
    <tspan x="30" dy="28">This often happens when Graphviz installed or not available on PATH.</tspan>
  </text>
  <text x="30" y="155" font-family="Courier New, monospace" font-size="15" fill="#333">
    {tspan_lines}
  </text>
</svg>"""


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

        try:
            powl_vis = powl_visualizer.apply(powl_model)
        except Exception as exc:
            LOGGER.warning("POWL visualization failed; returning fallback SVG.", exc_info=True)
            powl_vis = _exception_to_svg(exc)

        return petri_net_port_object, powl_vis