import knime.api.types as kt
import knime.extension as knext
import knime.extension.ports as kp
from typing import List, Dict, Any

class PetriNetPythonCell:
    def __init__(self, string_pn):
        pass


class PetriNetPythonFactory(kt.PythonValueFactory):
    def __init__(self):
        kt.PythonValueFactory.__init__(self, PetriNetPythonCell)


class PetriNetPortConverter(kp.PortObjectDecoder, kp.PortObjectEncoder):  
    def __init__(self):
        pass
