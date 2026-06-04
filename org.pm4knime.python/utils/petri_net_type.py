from pm4py.objects.petri_net.importer.variants.pnml import import_net_from_string
from pm4py.objects.petri_net.obj import PetriNet, Marking
from pm4py.objects.petri_net.utils import petri_utils
from pm4py.objects.petri_net.obj import PetriNet as Place
from pm4py.objects.petri_net.obj import PetriNet as Transition
from pm4py.objects.petri_net.obj import PetriNet as Arc
from pm4py.objects.petri_net.obj import PetriNet, Marking
import knime.api.types as kt
import knime.extension as knext
import knime.extension.ports as kp
from typing import List, Dict, Any
from dataclasses import dataclass
import json


class PetriNetPythonCell:
    def __init__(self, string_pn):
        self.net, self.initial_marking, self.final_marking = import_net_from_string(string_pn)
        self.stringPN = string_pn


class PetriNetPythonFactory(kt.PythonValueFactory):
    def __init__(self):
        kt.PythonValueFactory.__init__(self, PetriNetPythonCell)

    def decode(self, storage):
        return PetriNetPythonCell(storage)

    def encode(self, value):
        return value


def knime_value_factory(name):
    return '{"value_factory_class":"' + name + '"}'


_knime_value_factory = "org.pm4knime.node.conversion.pn2table.PetriNetCellFactory"

@dataclass
class Node:
    id: str
    type: str  
    label: str
    initial: bool = False  
    final: bool = False    
    
    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "type": self.type,
            "label": self.label,
            "initial": self.initial,
            "final": self.final
        }
    
    @staticmethod
    def from_dict(data: dict) -> "Node":
        return Node(
            id=data["id"],
            type=data["type"],
            label=data.get("label", ""),
            initial=data.get("initial", False),
            final=data.get("final", False)
        )


@dataclass
class Link:
    source: str
    target: str
    
    def to_dict(self) -> dict:
        return {
            "source": self.source,
            "target": self.target
        }
    
    @staticmethod
    def from_dict(data: dict) -> "Link":
        return Link(
            source=data["source"],
            target=data["target"]
        )


class PetriNetSpec(knext.PortObjectSpec):
    def serialize(self) -> dict:
        return {}
    
    @staticmethod
    def deserialize(data: dict) -> "PetriNetSpec":
        return PetriNetSpec()


class PetriNetPortObject(knext.PortObject):
    def __init__(self, spec: PetriNetSpec, nodes: List[Node], links: List[Link]):
        super().__init__(spec)
        self._nodes = nodes
        self._links = links
    
    @property
    def nodes(self) -> List[Node]:
        return self._nodes
    
    @property
    def links(self) -> List[Link]:
        return self._links
    
    @property
    def places(self) -> List[Node]:
        return [node for node in self._nodes if node.type == "place"]
    
    @property
    def transitions(self) -> List[Node]:
        return [node for node in self._nodes if node.type == "transition"]
    
    @property
    def initial_marking(self) -> List[Node]:
        return [node for node in self.places if node.initial]
    
    @property
    def final_marking(self) -> List[Node]:
        return [node for node in self.places if node.final]
    
    def get_summary(self) -> str:
        num_places = len(self.places)
        num_transitions = len(self.transitions)
        return f"Transitions: {num_transitions}, Places: {num_places}"
    
    def __repr__(self):
        return f"PetriNetPortObject({self.get_summary()})"
    
    def serialize(self) -> bytes:
        data = {
            "nodes": [node.to_dict() for node in self._nodes],
            "links": [link.to_dict() for link in self._links]
        }
        return json.dumps(data).encode()
    
    @classmethod
    def deserialize(cls, spec: PetriNetSpec, storage: bytes) -> "PetriNetPortObject":
        data = json.loads(storage.decode())
        
        nodes = [Node.from_dict(node_data) for node_data in data["nodes"]]
        links = [Link.from_dict(link_data) for link_data in data["links"]]
        
        return cls(spec, nodes, links)


class PetriNetPortConverter(
    kp.PortObjectDecoder[
        PetriNetPortObject,
        kp.StringIntermediateRepresentation,
        PetriNetSpec,
        kp.EmptyIntermediateRepresentation,
    ],
    kp.PortObjectEncoder[
        PetriNetPortObject,
        kp.StringIntermediateRepresentation,
        PetriNetSpec,
        kp.EmptyIntermediateRepresentation,
    ],
):
    
    def __init__(self):
        kp.PortObjectDecoder.__init__(self, PetriNetPortObject, PetriNetSpec)
        kp.PortObjectEncoder.__init__(self, PetriNetPortObject, PetriNetSpec)
    
    def decode_spec(self, intermediate_representation):
        return PetriNetSpec()
    
    def decode_object(
        self, 
        intermediate_representation: kp.StringIntermediateRepresentation, 
        spec: PetriNetSpec
    ) -> PetriNetPortObject:
        data = json.loads(intermediate_representation.getStringRepresentation())
        
        if "nodes" not in data or "links" not in data:
            raise ValueError("Expected JSON with 'nodes' and 'links' arrays")
        
        nodes = [Node.from_dict(node_data) for node_data in data["nodes"]]
        links = [Link.from_dict(link_data) for link_data in data["links"]]
        
        return PetriNetPortObject(spec, nodes, links)
    
    def encode_object(
        self, port_object: PetriNetPortObject
    ) -> kp.StringIntermediateRepresentation:
        data = {
            "nodes": [node.to_dict() for node in port_object.nodes],
            "links": [link.to_dict() for link in port_object.links]
        }
        return kp.StringIntermediateRepresentation(json.dumps(data))
    
    def encode_spec(self, spec: PetriNetSpec):
        return None
    
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
