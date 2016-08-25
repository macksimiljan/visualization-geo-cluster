package parser;

import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import general.ClusterRepresentative;
import general.Vertex;
import general.VertexDict;

public class MergedClusterParser {
	
	
	
	public MergedClusterParser() {
		
	}
	
	public ClusterRepresentative parseLine(String line) {
		JSONParser parser = new JSONParser();
		VertexDict dict = new VertexDict();
		ClusterRepresentative r = new ClusterRepresentative();
		
		try {
			JSONObject object = (JSONObject)parser.parse(line);
			r.id = (Long) object.get("id");
			JSONObject data = (JSONObject)object.get("data");
			r.label = (String)data.get("label");
			r.lat = (Float)data.get("lat");
			r.lon = (Float)data.get("lon");
			r.ontologies = new HashSet<String>();
			JSONArray ontologies = (JSONArray)data.get("ontologies");
			Iterator it = ontologies.iterator();
			while (it.hasNext()) {
				String ontology = (String)it.next();
				r.ontologies.add(ontology);
			}
			r.typeIntern = new HashSet<String>();
			JSONArray typeInterns = (JSONArray)data.get("typeIntern");
			it = typeInterns.iterator();
			while (it.hasNext()) {
				String type = (String)it.next();
				r.typeIntern.add(type);
			}
			r.clusteredVertices = new HashSet<Vertex>();
			JSONArray vertices = (JSONArray)data.get("clusteredVertices");
			it = vertices.iterator();
			while (it.hasNext()) {
				Long id = (Long)it.next();
				r.clusteredVertices.add(dict.getVertexById(id));
			}
			
 		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return r;
	}

}
