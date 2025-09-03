import knime.extension as knext
import pandas as pd
import os
import logging
import pytz
from pm4py.algo.discovery.powl.inductive.variants.powl_discovery_varaints import POWLDiscoveryVariant
from pm4py.visualization.powl.visualizer import apply as visualize_powl
from pm4py.algo.discovery.powl import algorithm as powl_disc
from pm4py.objects.conversion.powl.converter import apply as powl_to_pn
from utils import knime_util
from utils.petri_net_type import petri_net_to_df


LOGGER = logging.getLogger(__name__)

script_dir = os.path.dirname(os.path.abspath(__file__))
path_to_icon = os.path.abspath(os.path.join(script_dir, "..", "icon", "category-discovery.png"))


@knext.node(name="POWL Miner",
            node_type=knext.NodeType.LEARNER,
            icon_path=path_to_icon,
            category="/community/processmining/discovery")
@knime_util.create_node_description(
    short_description="Discover a Partially Ordered Workflow Model (POWL) from an event table.",
    description="Discover a Partially Ordered Workflow Model (POWL) from an event log.")
@knext.input_table(name="Event Table", description="An Event Table.")
@knext.output_table(name="Petri Net Table", description="A Petri net Table")
@knext.output_image(name="POWL Model", description="An SVG image of a POWL model.")
@knext.output_view(name="POWL Model", description="A POWL model.")
class POWL_Miner:
    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_activity = knext.ColumnParameter(label="Activity Column",
                                                  description="The column that contains the activities.",
                                                  port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps."
                                                          "This column must have the type 'Local Date Time'.",
                                              port_index=0,
                                              column_filter=knime_util.is_date)

    def configure(self, configure_context: knext.ConfigurationContext, input_schema_1: knext.Schema):
        for par in [self.column_param_case, self.column_param_time, self.column_param_activity]:
            if par is None or par == "":
                raise ValueError("Parameters not set!")
        return None

    def execute(self, exec_context, input_1):
        event_log = input_1.to_pandas()
        event_log["time:timestamp"] = pd.to_datetime(event_log[self.column_param_time],
                                                     format='%Y-%m-%d %H:%M:%S').dt.tz_localize(pytz.utc)
        event_log.rename(
            columns={self.column_param_case: 'case:concept:name', self.column_param_activity: 'concept:name'},
            inplace=True)

        event_log = event_log.sort_values(by=["case:concept:name", "time:timestamp"])
        powl = powl_disc.apply(event_log, variant=POWLDiscoveryVariant.MAXIMAL)
        pn_1, init_1, final_1 = powl_to_pn(powl)
        pn_df = petri_net_to_df(pn_1, init_1, final_1)

        powl_vis = visualize_powl(powl, parameters={"format": "svg"})

        return knext.Table.from_pandas(pn_df), powl_vis, knext.view_svg(powl_vis)

