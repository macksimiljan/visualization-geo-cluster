package parser;

import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import representation.ClusterRepresentative;

/**
 * Parses data in 'mergedCluster.geojson'.
 * 
 * @author MM
 *
 */
public class MergedClusterParser {
	
	/**
	 * Parses a string representing a JSON object as a cluster representative object.
	 * @param line JSON object of a cluster representative.
	 * @return Cluster representative.
	 * @throws ParseException 
	 */
	@SuppressWarnings("rawtypes")
	public ClusterRepresentative parseLine(String line) throws ParseException {
		JSONParser parser = new JSONParser();
		ClusterRepresentative r = new ClusterRepresentative();
		
		JSONObject object = (JSONObject)parser.parse(line);
		r.id = (Long) object.get("id");
		JSONObject data = (JSONObject)object.get("data");
		r.label = (String)data.get("label");
		r.simpleType = (String)data.get("simpleType");
		try {
			r.lat = (Double)data.get("lat");
		} catch(Exception e) {
			if (data.get("lat") != null) {
				r.lat = ((Long)data.get("lat")).doubleValue();
			}
		}
		try {
			r.lon = (Double)data.get("lon");
		} catch(Exception e) {
			if (data.get("lon") != null) {
				r.lon = ((Long)data.get("lon")).doubleValue();
			}
		}
		r.ontologies = new HashSet<String>();
		JSONArray ontologies = (JSONArray)data.get("ontologies");
		Iterator it = ontologies.iterator();
		while (it.hasNext()) {
			String ontology = (String)it.next();
			r.ontologies.add(ontology);
		}
		r.typeIntern = new HashSet<String>();
		JSONArray typeInterns = (JSONArray)data.get("typeIntern");
		if (typeInterns != null) {
			it = typeInterns.iterator();
			while (it.hasNext()) {
				String type = (String)it.next();
				r.typeIntern.add(type);
			}
		}
		r.clusteredVertexIds = new HashSet<Long>();
		JSONArray vertices = (JSONArray)data.get("clusteredVertices");
		it = vertices.iterator();
		while (it.hasNext()) {
			Long id = (Long)it.next();
			r.clusteredVertexIds.add(id);
		}
			
 		
		
		return r;
	}
	
	public static String printClusterRepresentative(ClusterRepresentative r) {
		JSONObject obj = new JSONObject();
		obj.put("id", r.id);
		JSONObject data = new JSONObject();
		JSONArray typeIntern = new JSONArray();
		for (String type : r.typeIntern)
			typeIntern.add(type);
		data.put("typeIntern", typeIntern);
		data.put("simpleType", r.simpleType);
		data.put("lon", r.lon);
		data.put("label", r.label);
		data.put("lat", r.lat);
		JSONArray ontologies = new JSONArray();
		for (String ont : r.ontologies)
			ontologies.add(ont);
		data.put("ontologies", ontologies);
		JSONArray clusteredVertices = new JSONArray();
		for (Long vertex : r.clusteredVertexIds)
			clusteredVertices.add(vertex);
		data.put("clusteredVertices", clusteredVertices);
		obj.put("data", data);
		
		return obj.toJSONString();
	}

}
