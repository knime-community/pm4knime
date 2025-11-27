package org.pm4knime.node.visualizations.jsgraphviz;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.knime.base.node.mine.decisiontree2.image.DecTreeToImageNodeFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.gson.Gson;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JSGraphVizViewRepresentation extends JSONViewContent {
	
	

	public final int pseudoIdentifier = (new Random()).nextInt();
	DecTreeToImageNodeFactory f;
	
	private static final String JSON = "json";
	private String json;

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		try {
			settings.addString(JSON, json);
	    } catch (Exception ex) {
	        // do nothing
	    }   
	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			json = settings.getString(JSON);
	    } catch (Exception ex) {
	        // do nothing
	    }   
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		JSGraphVizViewRepresentation other = (JSGraphVizViewRepresentation)obj;
		return Objects.equals(json, other.json);
	}

	@Override
	public int hashCode() {
		return Objects.hash(json);
	}

	
	public String getJson() {
		return json;
	}
	
	public void setJSONString(Map<String, List<?>> json) {
		
		Gson gson = new Gson();
        String jsonData = gson.toJson(json);
		this.json = jsonData;
		
		System.out.println("json data on the java side");
		System.out.println(json);
		System.out.println(jsonData);
		
	}
}

