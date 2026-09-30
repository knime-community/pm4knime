import knime.api.types as kt
import knime.extension as knext
import knime.extension.ports as kp
from typing import List, Dict, Any
from dataclasses import dataclass
import json

class PetriNetPythonCell:
    def __init__(self, string_pn):
        pass


class PetriNetPythonFactory(kt.PythonValueFactory):
    def __init__(self):
        kt.PythonValueFactory.__init__(self, PetriNetPythonCell)


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
