from pm4py.objects.petri_net.obj import PetriNet, Marking
from pm4py.objects.petri_net.utils import petri_utils
from pm4py.objects.petri_net.obj import PetriNet as Place
from pm4py.objects.petri_net.obj import PetriNet as Transition
from pm4py.objects.petri_net.obj import PetriNet as Arc
from pm4py.objects.petri_net.obj import PetriNet, Marking
from pm4knime_utils.petri_net_type import Node, Link, PetriNetSpec, PetriNetPortObject


def convert_port_object_to_pm4py(petri_net_data):    
    if isinstance(petri_net_data, dict):
        petri_net_json_data = petri_net_data
    elif hasattr(petri_net_data, 'nodes') and hasattr(petri_net_data, 'links'):
        petri_net_json_data = {
            "nodes": [node.to_dict() for node in petri_net_data.nodes],
            "links": [link.to_dict() for link in petri_net_data.links]
        }
    else:
        try:
            if hasattr(petri_net_data, 'getJSON'):
                petri_net_json_data = petri_net_data.getJSON()
            else:
                raise TypeError(f"Expected either a PetriNetPortObject or dictionary, got {type(petri_net_data)}")
        except Exception as e:
            raise TypeError(f"Failed to process input: {e}")

    if "nodes" not in petri_net_json_data or "links" not in petri_net_json_data:
        raise ValueError("Invalid Petri net JSON structure: missing 'nodes' or 'links' keys")

    net = PetriNet()
    initial_marking = Marking()
    final_marking = Marking()
    pm4py_elements = {}

    for node_data in petri_net_json_data["nodes"]:
        node_id = node_data["id"]
        node_type = node_data["type"]

        if node_type == "place":
            place = PetriNet.Place(node_id)
            net.places.add(place)
            pm4py_elements[node_id] = place

            if node_data.get("initial", False):
                initial_marking[place] = 1
            if node_data.get("final", False):
                final_marking[place] = 1

        elif node_type == "transition":
            label = node_data.get("label", "")
            transition = PetriNet.Transition(node_id, label if label else None)
            net.transitions.add(transition)
            pm4py_elements[node_id] = transition

    for link_data in petri_net_json_data["links"]:
        source_id = link_data["source"]
        target_id = link_data["target"]
        source_obj = pm4py_elements.get(source_id)
        target_obj = pm4py_elements.get(target_id)

        if source_obj and target_obj:
            arc = PetriNet.Arc(source_obj, target_obj, weight=1)
            net.arcs.add(arc)
            source_obj.out_arcs.add(arc)
            target_obj.in_arcs.add(arc)

    return net, initial_marking, final_marking


def convert_pm4py_to_port_object(net, initial_marking, final_marking):
    nodes = []
    links = []
        
    for place in net.places:
        is_initial = place in initial_marking and initial_marking[place] > 0
        is_final = place in final_marking and final_marking[place] > 0
        
        node = Node(
            id=place.name,
            type="place",
            label="",
            initial=is_initial,
            final=is_final
        )
        nodes.append(node)
    
    for transition in net.transitions:
        if transition.label is None or transition.label == "None":
            display_label = ""
        else:
            display_label = transition.label
        
        node = Node(
            id=transition.name,
            type="transition",
            label=display_label,
            initial=False,
            final=False
        )
        nodes.append(node)
    
    for arc in net.arcs:
        link = Link(
            source=arc.source.name,
            target=arc.target.name
        )
        links.append(link)
    
    spec = PetriNetSpec()
    return PetriNetPortObject(spec, nodes, links)
