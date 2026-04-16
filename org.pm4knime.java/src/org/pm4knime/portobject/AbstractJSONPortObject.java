package org.pm4knime.portobject;

import java.util.List;
import java.util.Map;

import org.knime.core.node.port.AbstractPortObject;


public abstract class AbstractJSONPortObject extends AbstractPortObject {
	
	public static class Node {
        String id;
        String type;
        String label; 

        public Node(String id, String type, String label) {
            this.id = id;
            this.type = type;
            this.label = label; 
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }
    }
	
	public static class PlaceNode extends Node {

        boolean i_marking;
        boolean f_marking;

        public PlaceNode(String id, String type, String label, boolean initial_marking, boolean final_marking) {
        	super(id, type, label);
            this.i_marking = initial_marking;
            this.f_marking = final_marking;
        }

        public boolean getI_marking() {
            return i_marking;
        }

        public boolean getF_marking() {
            return f_marking;
        }
    }

    public static class Link {
        String source;
        String target;
        String type = "";

        public Link(String source, String target) {
            this.source = source;
            this.target = target;
        }
        
        public Link(String source, String target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getType() {
            return type;
        }
    }
    
    public static class LinkWithFrequency {
        String source;
        String target;
        int frequency;

        public LinkWithFrequency(String source, String target, int frequency) {
            this.source = source;
            this.target = target;
            this.frequency = frequency;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public int getFrequency() {
            return frequency;
        }
    }
	
	public abstract Map<String, List<?>> getJSON();

}
