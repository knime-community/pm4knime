package org.pm4knime.node.conversion.pn2bpmn;

import org.knime.node.DefaultNode;
import org.knime.node.DefaultNodeFactory;
import org.pm4knime.node.visualizations.common.ModernViews;
import org.pm4knime.portobject.BpmnPortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.util.defaultnode.EmptyNodeSettings;

public class PN2BPMNConverterNodeFactory extends DefaultNodeFactory {

    public PN2BPMNConverterNodeFactory() {
        super(
            DefaultNode.create()
                .name("Petri Net to BPMN")
                .icon("icon/category-conversion.png")
                .shortDescription("Convert a Petri net into a BPMN.")
                .fullDescription(
                    "This node converts a Petri net into a bpmn model. No configuration is needed to complete the "
                        + "conversion.<br />\r\n"
                        + "         <br /> \r\n"
                        + "   		<b>About PN2BPMN Converter:</b>\r\n"
                        + "   			This KNIME node converts a Petri net representation to a BPMN (Business Process "
                        + "Model and Notation) representation. The code implements the logic for the conversion "
                        + "process, handling various aspects of the Petri net, such as places, transitions, arcs, "
                        + "and markings. The conversion involves creating a BPMN diagram and mapping elements "
                        + "between the Petri net and BPMN representations. This node also addresses specific cases, "
                        + "such as handling transitions without incoming flows, removing dead places, and ensuring a "
                        + "proper conversion. It utilizes the Process Mining (PM4Knime) and KNIME APIs for handling "
                        + "Petri net and BPMN data structures. Additionally, the code of this node includes methods "
                        + "for handling free-choice nets, simplifying BPMN diagrams, and managing initial and final "
                        + "markings. The node model is integrated into the KNIME workflow environment, and it outputs "
                        + "a BPMN representation as a result.\r\n"
                        + "   		<br />\r\n"
                        + "    	<br />  \r\n"
                        + "   		<b>Petri Net:</b><br /> A Petri net is a directed bipartite graph that visualizes "
                        + "all possible traces (order of executed activities). A Petri net consists of places, "
                        + "transitions, and directed arcs that connect them. Arcs can either connect places with "
                        + "transitions or transitions with places. Places can contain tokens. The placement of tokens "
                        + "defines the state of the Petri net. There is a start and a final state. The start state "
                        + "has only a token in the green place (initial place). The goal state has only a token in "
                        + "the double margin place (final place). A place is enabled if it it contains at least one "
                        + "token. A transition can only fire if all incoming places are enabled. After firing a "
                        + "transition, a token is consumed from all of its incoming places, and a token is produced "
                        + "in all of its outgoing places.\r\n"
                        + "    	<br /> \r\n"
                        + "     	<br /> \r\n"
                        + "     	<b>BPMN:</b><br />\r\n"
                        + "		    BPMN, or Business Process Model and Notation, encompasses several key elements "
                        + "that collectively define and illustrate a business process. Activities, depicted as "
                        + "rectangular shapes, symbolize the actual work within the process. Events signify "
                        + "occurrences during the process, with Start Events represented by green circles, End Events "
                        + "by red circles with bold borders, and Intermediate Events by circles with double borders.\r\n"
                        + "		\r\n"
                        + "		    Gateways, visualized as diamond shapes, play a crucial role in controlling the "
                        + "flow of execution within a process. They facilitate decision-making, split the flow into "
                        + "multiple paths, or merge paths into a single one. Various Gateway Types include EXCLUSIVE "
                        + "(âœ•), PARALLEL (âœš), COMPLEX (â‹), INCLUSIVE (â—¯), and EVENTBASED (âŒ¾).\r\n"
                        + "		\r\n"
                        + "			Flows, crucial for sequencing activities, delineate the order and connections "
                        + "between activities. These elements collectively provide a comprehensive visualization of "
                        + "business processes in BPMN, ensuring clarity in understanding and managing workflows")
                .sinceVersion(3, 0, 0)
                .ports(p -> p
                    .addInputPort("Petri net", "a Petri net", PetriNetPortObject.TYPE)
                    .addOutputPort("BPMN", "a Business Process Modeling Notation (BPMN)", BpmnPortObject.TYPE))
                .model(m -> m
                    .parametersClass(EmptyNodeSettings.class)
                    .configure(PN2BPMNConverterNodeModel::configure)
                    .execute(PN2BPMNConverterNodeModel::execute))
                .addView(v -> ModernViews.bpmn(v, PN2BPMNConverterNodeFactory.class, "BPMN view"))
                .nodeType(NodeType.Manipulator));
    }
}
