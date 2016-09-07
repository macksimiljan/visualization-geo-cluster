package parser;

import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import representation.Vertex;

/**
 * Parses data in 'vertexInput.geojson'.
 * 
 * @author MM
 *
 */
public class VertexInputParser {
	
	
	/**
	 * Parses a string representing a JSON object as an InputEdge object.
	 * @param line JSON object of a vertex.
	 * @return Vertex.
	 */
	@SuppressWarnings("rawtypes")
	public Vertex parseLine(String line) {
		JSONParser parser = new JSONParser();
		Vertex v = new Vertex();
		
		try {
			JSONObject object = (JSONObject)parser.parse(line);
			v.id = (Long)object.get("id");
			JSONObject data = (JSONObject)object.get("data");
			v.label = (String)data.get("label");
			try {
				v.lat = (Double)data.get("lat");
			} catch(Exception e) {
				if (data.get("lat") != null) {
					v.lat = ((Long)data.get("lat")).doubleValue();
				}
			}
			try {
				v.lon = (Double)data.get("lon");
			} catch(Exception e) {
				if (data.get("lon") != null) {
					v.lon = ((Long)data.get("lon")).doubleValue();
				}
			}
			v.ontology = (String)data.get("ontology");
			v.typeInternInput = new HashSet<String>();
			JSONArray typeInterns = (JSONArray)data.get("typeIntern");
			Iterator it = typeInterns.iterator();
			while (it.hasNext()) {
				String type = (String)it.next();
				v.typeInternInput.add(type);
			}
			v.url = (String)data.get("url");
			
			
 		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return v;
	}

}
